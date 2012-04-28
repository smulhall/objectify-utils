/**
 * This code is licensed by Ted Stockwell per this thread here:
 * http://groups.google
 * .com/group/google-appengine-java/browse_thread/thread/684b6aa3e4966e0b
 * /f1a4a22ca667b569?lnk=gst&q=bigdecimal#f1a4a22ca667b569
 * 
 * On Sep 21, 3:05 am, Philippe Marschall <philippe.marsch...@gmail.com> wrote:
 * 
 * > But long is crappy abstraction. Sometimes you need two decimal places<br>
 * > sometimes three, sometimes six, sometimes "as many as there are". <br>
 * > That's all quite cumbersome to do with a long alone. String seems like <br>
 * > the easier way to go.
 * 
 * Well, in case anyone is interested, below I have included the code for a
 * class that can lexicographically encode BigDecimals as strings (and I have
 * included JUnit test class). This class encodes numbers as strings in such a
 * way that the lexicographic order of the encoded strings is the same as the
 * natural order of the original numbers. The length of an encoded number is
 * only slightly larger than the length of its original number. Unlike other
 * schemes, there is no limit to the size of numbers which may be encoded. This
 * encoding lets you store BigDecimals as Strings and still be able to do proper
 * range searches in queries.
 * 
 * This code was based on ideas in this paper: www.zanopha.com/docs/elen.ps ,
 * but there are some minor differences. Feel free to use this code as you wish.
 * 
 */
package com.sappenin.objectify.translate.util;

import java.math.BigDecimal;
import java.text.ParseException;

/**
 * Encodes numbers as strings in such a way that the lexicographic order of the
 * encoded strings is the same as the natural order of the original numbers. The
 * length of an encoded number is only slightly larger than the length of its
 * original number. Unlike other schemes, there is no limit to the size of
 * numbers which may be encoded.
 * 
 * @author ted stockwell
 * 
 */
public class BigDecimalCodec
{

	/**
	 * 
	 * @param input
	 * @return
	 */
	public static final String decode(String input)
	{
		try
		{
			if (input == null)
			{
				return null;
			}
			if (input.length() <= 0)
			{
				return "";
			}
			return new Decoder(input)._output;
		}
		catch (ParseException e)
		{
			throw new RuntimeException("Failed to decode number:" + input, e);
		}
	}

	/**
	 * 
	 * @param input
	 * @return
	 */
	public static final BigDecimal decodeAsBigDecimal(String input)
	{
		try
		{
			if (input == null)
			{
				return null;
			}
			if (input.length() <= 0)
			{
				throw new RuntimeException("Internal Error: Cannot decode an empty String");
			}
			return new BigDecimal(new Decoder(input)._output);
		}
		catch (ParseException e)
		{
			throw new RuntimeException("Failed to decode number:" + input, e);
		}
	}

	/**
	 * 
	 * @param input
	 * @return
	 */
	public static final String encode(String input)
	{
		try
		{
			if (input == null)
			{
				return null;
			}
			if (input.length() <= 0)
			{
				return "";
			}
			return new Encoder(input)._output;
		}
		catch (ParseException e)
		{
			throw new RuntimeException("Failed to parse number:" + input, e);
		}
	}

	/**
	 * 
	 * @param decimal
	 * @return
	 */
	public static final String encode(BigDecimal decimal)
	{
		if (decimal == null)
		{
			return null;
		}
		return BigDecimalCodec.encode(decimal.toPlainString());
	}

	/**
	 * 
	 *
	 */
	static public class Encoder
	{

		private String _input;
		private int _position = 0;
		private int _end;
		private String _output = "";
		private boolean _isNegative = false;

		private Encoder(String input) throws ParseException
		{
			this._input = input;
			this._end = this._input.length();

			char c = this._input.charAt(this._position);
			if (c == '-')
			{
				this._input.charAt(this._position++);
				this._isNegative = true;
			}

			this.readNumberBeforeDecimal();
			if (this.readDecimalPoint())
			{
				this.readNumber(this._end - this._position);
			}
			this._output += this._isNegative ? '?' : '*';
		}

		/**
		 * 
		 * @return
		 * @throws ParseException
		 */
		private boolean readDecimalPoint() throws ParseException
		{
			if (this._end <= this._position)
			{
				return false;
			}
			char c = this._input.charAt(this._position++);
			if (c != '.')
			{
				this.throwParseException("Expected decimal point");
			}
			if (this._end <= this._position)
			{
				return false;
			}
			this._output += this._isNegative ? ':' : '.';
			return true;
		}

		/**
		 * 
		 * @throws ParseException
		 */
		private void readNumberBeforeDecimal() throws ParseException
		{
			char[] buffer = new char[this._input.length()];

			// read number until decimal point reached or end
			int i = 0;
			while (this._end > this._position)
			{
				char c = this._input.charAt(this._position++);
				if (('0' <= c) && (c <= '9'))
				{
					buffer[i++] = (char) (this._isNegative ? '0' + ('9' - c) : c);
				}
				else if (c == '.')
				{
					this._position--;
					break;
				}
			}

			// now figure out needed prefixes
			String prefix = "";
			int l = i;
			String unaryPrefix = this._isNegative ? "*" : "?";
			while (1 < l)
			{
				unaryPrefix += this._isNegative ? '*' : '?';
				String s = Integer.toString(l);
				if (this._isNegative)
				{
					char[] cs = s.toCharArray();
					for (int j = 0; j < cs.length; j++)
					{
						cs[j] = (char) (('0' + '9') - cs[j]);
					}
					s = new String(cs);
				}
				prefix = s + prefix;
				l = s.length();
			}

			this._output += unaryPrefix; // output unary prefix count
			this._output += prefix; // output prefixes
			this._output += new String(buffer, 0, i); // now output actual
														// number
		}

		/**
		 * 
		 * @param length
		 */
		private void readNumber(int length)
		{
			if (this._isNegative)
			{
				while (0 < length--)
				{
					this._output += (char) ('0' + ('9' - this._input.charAt(this._position++)));
				}
			}
			else
			{
				this._output += this._input.substring(this._position, this._position + length);
				this._position += length;
			}
		}

		/**
		 * 
		 * @param message
		 * @throws ParseException
		 */
		private void throwParseException(String message) throws ParseException
		{
			throw new ParseException(message, this._position);
		}
	}

	/**
	 *
	 */
	static public class Decoder
	{

		private String _input;
		private int _position = 0;
		private int _end;
		private String _output = "";
		private boolean _isNegative = false;

		/**
		 * 
		 * @param input
		 * @throws ParseException
		 */
		private Decoder(String input) throws ParseException
		{
			this._input = input;
			this._end = this._input.length();
			int lastChar = this._input.charAt(this._end - 1);
			while ((lastChar == '*') || (lastChar == '?') || (lastChar == ':') || (lastChar == '.'))
			{
				lastChar = this._input.charAt((--this._end) - 1);
			}

			char c = this._input.charAt(this._position);
			if (c == '*')
			{
				this._output += '-';
				this._isNegative = true;
			}
			else if (c != '?')
			{
				throw new ParseException("All encoded numbers must begin with either '?' or '*'", this._position);
			}

			this.readSequence();
			if (this.readDecimalPoint())
			{
				this.readNumber(this._end - this._position);
			}
		}

		/**
		 * 
		 * @return
		 * @throws ParseException
		 */
		private boolean readDecimalPoint() throws ParseException
		{
			if (this._end <= this._position)
			{
				return false;
			}
			char c = this._input.charAt(this._position++);
			if (c != (this._isNegative ? ':' : '.'))
			{
				throw new ParseException("Expected decimal point", this._position);
			}
			if (this._end <= this._position)
			{
				return false;
			}
			this._output += '.';
			return true;
		}

		/**
		 * 
		 * @throws ParseException
		 */
		private void readSequence() throws ParseException
		{
			int sequenceCount = 0;
			while (true)
			{
				int c = this._input.charAt(this._position++);
				if ((c == '*') || (c == '?'))
				{
					sequenceCount++;
				}
				else
				{
					this._position--;
					break;
				}
			}
			this.readNumberSequence(sequenceCount);
		}

		/**
		 * 
		 * @param sequenceCount
		 */
		private void readNumberSequence(int sequenceCount)
		{
			int prefixLength = 1;
			while (1 < sequenceCount--)
			{
				prefixLength = this.readPrefix(prefixLength);
			}
			this.readNumber(prefixLength);
		}

		/**
		 * 
		 * @param length
		 * @return
		 */
		private int readPrefix(int length)
		{
			String s;
			if (this._isNegative)
			{
				char[] cs = new char[length];
				int i = 0;
				while (0 < length--)
				{
					cs[i++] = (char) ('0' + ('9' - this._input.charAt(this._position++)));
				}
				s = new String(cs);
			}
			else
			{
				s = this._input.substring(this._position, this._position + length);
				this._position += length;
			}
			return Integer.parseInt(s);
		}

		/**
		 * 
		 * @param length
		 */
		private void readNumber(int length)
		{
			if (this._isNegative)
			{
				while (0 < length--)
				{
					this._output += (char) ('0' + ('9' - this._input.charAt(this._position++)));
				}
			}
			else
			{
				this._output += this._input.substring(this._position, this._position + length);
				this._position += length;
			}
		}
	}

}