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
 *
 */
package net.sourceforge.plantuml.timingdiagram.command;

import net.sourceforge.plantuml.command.CommandExecutionResult;
import net.sourceforge.plantuml.command.SingleLineCommand2;
import net.sourceforge.plantuml.klimt.color.ColorParser;
import net.sourceforge.plantuml.klimt.color.ColorType;
import net.sourceforge.plantuml.klimt.color.Colors;
import net.sourceforge.plantuml.klimt.color.NoSuchColorException;
import net.sourceforge.plantuml.regex.IRegex;
import net.sourceforge.plantuml.regex.RegexLeaf;
import net.sourceforge.plantuml.regex.RegexOr;
import net.sourceforge.plantuml.regex.RegexResult;
import net.sourceforge.plantuml.timingdiagram.Player;
import net.sourceforge.plantuml.timingdiagram.TimeTick;
import net.sourceforge.plantuml.timingdiagram.TimingDiagram;

abstract class CommandChangeState extends SingleLineCommand2<TimingDiagram> {

	CommandChangeState(IRegex pattern) {
		super(pattern);
	}

	static final String STATE_CODE = "([-%pLN_][-%pLN_.]*)";

	static ColorParser color() {
		return ColorParser.simpleColor(ColorType.BACK);
	}

	protected CommandExecutionResult addState(TimingDiagram diagram, RegexResult arg, final Player player,
			final TimeTick now) throws NoSuchColorException {
		final String comment = arg.get("COMMENT", 0);
		final Colors colors = color().getColor(arg, diagram.getSkinParam().getIHtmlColorSet());
		player.setState(now, comment, colors, getStates(arg));
		return CommandExecutionResult.ok();
	}

	private String[] getStates(RegexResult arg) {
		if (arg.get("STATE7", 0) != null) {
			final String state1 = arg.get("STATE7", 0);
			final String state2 = arg.get("STATE7", 1);
			return new String[] { state1, state2 };
		}
		return new String[] { arg.getLazzy("STATE", 0) };
	}

	static IRegex getStateOrHidden() {
		return new RegexOr(//
				new RegexLeaf(1, "STATE1", "[%g]([^%g]*)[%g]"), //
				new RegexLeaf(1, "STATE2", STATE_CODE), //
				new RegexLeaf(1, "STATE3", "(\\{hidden\\})"), //
				new RegexLeaf(1, "STATE4", "(\\{\\.\\.\\.\\})"), //
				new RegexLeaf(1, "STATE5", "(\\{-\\})"), //
				new RegexLeaf(1, "STATE6", "(\\{\\?\\})"), //
				new RegexLeaf(2, "STATE7", "(?:\\{" + STATE_CODE + "," + STATE_CODE + "\\})") //
		);
	}

}
