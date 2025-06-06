/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2024, Arnaud Roques
 *
 * Project Info:  https://plantuml.com
 *
 * If you like this project or if you find it useful, you can support us at:
 *
 * https://plantuml.com/patreon (only 1$ per month!)
 * https://plantuml.com/paypal
 *
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 *
 */
package net.sourceforge.plantuml.tim.expression;

import net.sourceforge.plantuml.text.TLineType;
import net.sourceforge.plantuml.tim.Eater;
import net.sourceforge.plantuml.tim.EaterException;

public enum TokenType {
	// ::remove folder when __HAXE__
	QUOTED_STRING, JSON_DATA, OPERATOR, OPEN_PAREN_MATH, COMMA, CLOSE_PAREN_MATH, NUMBER, PLAIN_TEXT, SPACES,
	FUNCTION_NAME, OPEN_PAREN_FUNC, CLOSE_PAREN_FUNC, AFFECTATION;

	public static final char COMMERCIAL_MINUS_SIGN = '\u2052';

	private boolean isSingleChar1() {
		return this == OPEN_PAREN_MATH || this == COMMA || this == CLOSE_PAREN_MATH;
	}

	private static boolean isPlainTextBreak(char ch, char ch2) {
		final TokenType tmp = fromChar(ch, ch2);
		if (tmp.isSingleChar1() || tmp == TokenType.OPERATOR || tmp == SPACES || tmp == AFFECTATION)
			return true;

		return false;
	}

	private static TokenType fromChar(char ch, char ch2) {
		TokenType result = PLAIN_TEXT;
		if (TLineType.isQuote(ch))
			result = QUOTED_STRING;
		else if (ch == '=')
			result = AFFECTATION;
		else if (ch == '(')
			result = OPEN_PAREN_MATH;
		else if (ch == ')')
			result = CLOSE_PAREN_MATH;
		else if (ch == ',')
			result = COMMA;
		else if (TLineType.isLatinDigit(ch))
			result = NUMBER;
		else if (TLineType.isSpaceChar(ch))
			result = SPACES;
		else if (TokenOperator.getTokenOperator(ch, ch2) != null)
			result = OPERATOR;

		return result;
	}

	final static public Token eatOneToken(Token lastToken, Eater eater, boolean manageColon) throws EaterException {
		char ch = eater.peekChar();
		if (ch == 0)
			return null;

		if (manageColon && ch == ':')
			return null;

		if (ch == '-' && isTheMinusAnOperation(lastToken))
			ch = COMMERCIAL_MINUS_SIGN;

		final TokenOperator tokenOperator = TokenOperator.getTokenOperator(ch, eater.peekCharN2());
		if (TLineType.isQuote(ch)) {
			return new Token(eater.eatAndGetQuotedString(), TokenType.QUOTED_STRING, null);
		} else if (tokenOperator != null) {
			if (tokenOperator.getDisplay().length() == 1) {
				eater.eatOneChar();
				return new Token(ch, TokenType.OPERATOR, null);
			}

			return new Token("" + eater.eatOneChar() + eater.eatOneChar(), TokenType.OPERATOR, null);
		} else if (ch == '=') {
			return new Token(eater.eatOneChar(), TokenType.AFFECTATION, null);
		} else if (ch == '(') {
			return new Token(eater.eatOneChar(), TokenType.OPEN_PAREN_MATH, null);
		} else if (ch == ')') {
			return new Token(eater.eatOneChar(), TokenType.CLOSE_PAREN_MATH, null);
		} else if (ch == ',') {
			return new Token(eater.eatOneChar(), TokenType.COMMA, null);
		} else if (TLineType.isLatinDigit(ch) || ch == '-') {
			return new Token(eater.eatAndGetNumber(), TokenType.NUMBER, null);
		} else if (TLineType.isSpaceChar(ch)) {
			return new Token(eater.eatAndGetSpaces(), TokenType.SPACES, null);
		}
		return new Token(eatAndGetTokenPlainText(eater), TokenType.PLAIN_TEXT, null);
	}

	private static boolean isTheMinusAnOperation(Token lastToken) {
		if (lastToken == null)
			return false;
		final TokenType type = lastToken.getTokenType();
		if (type == TokenType.OPERATOR || type == TokenType.OPEN_PAREN_MATH || type == TokenType.COMMA
				|| type == TokenType.AFFECTATION)
			return false;
		return true;
	}

	static private String eatAndGetTokenPlainText(Eater eater) throws EaterException {
		final StringBuilder result = new StringBuilder();
		while (true) {
			final char ch = eater.peekChar();
			if (ch == 0 || TokenType.isPlainTextBreak(ch, eater.peekCharN2()))
				return result.toString();

			result.append(eater.eatOneChar());
		}
	}

}
