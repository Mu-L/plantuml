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
package net.sourceforge.plantuml.tim;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.sourceforge.plantuml.text.StringLocated;
import net.sourceforge.plantuml.tim.expression.TValue;
import net.sourceforge.plantuml.utils.Log;

public class TMemoryGlobal extends ExecutionContexts implements TMemory {

	private final Map<String, TValue> globalVariables = new HashMap<String, TValue>();
	private final TrieImpl variables = new TrieImpl();

	@Override
	public TValue getVariable(String varname) {
		return this.globalVariables.get(varname);
	}

	@Override
	public void dumpDebug(String message) {
		Log.error("[MemGlobal] Start of memory_dump " + message);
		dumpMemoryInternal();
		Log.error("[MemGlobal] End of memory_dump");
	}

	void dumpMemoryInternal() {
		Log.error("[MemGlobal] Number of variable(s) : " + globalVariables.size());
		for (Entry<String, TValue> ent : new TreeMap<String, TValue>(globalVariables).entrySet()) {
			final String name = ent.getKey();
			final TValue value = ent.getValue();
			Log.error("[MemGlobal] " + name + " = " + value);
		}
	}

	@Override
	public void putVariable(String varname, TValue value, TVariableScope scope, StringLocated location)
			throws EaterException {
		Log.info(() -> "[MemGlobal] Setting " + varname);
		if (scope == TVariableScope.LOCAL)
			throw new EaterException("Cannot use local variable here", location);

		this.globalVariables.put(varname, value);
		this.variables.add(varname);
	}

	@Override
	public void removeVariable(String varname) {
		this.globalVariables.remove(varname);
		this.variables.remove(varname);
	}

	@Override
	public boolean isEmpty() {
		return globalVariables.isEmpty();
	}

	@Override
	public Set<String> variablesNames() {
		return Collections.unmodifiableSet(globalVariables.keySet());
	}

	@Override
	public Trie variablesNames3() {
		return variables;
	}

	@Override
	public TMemory forkFromGlobal(Map<String, TValue> input) {
		return new TMemoryLocal(this, input);
	}

}
