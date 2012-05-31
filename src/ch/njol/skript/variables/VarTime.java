/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011, 2012 Peter Güttinger
 * 
 */

package ch.njol.skript.variables;

import org.bukkit.World;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Getter;
import ch.njol.skript.data.DefaultChangers;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.SimpleVariable;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.Time;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class VarTime extends SimpleVariable<Time> {
	
	static {
		Skript.registerVariable(VarTime.class, Time.class, "time [(in|of) %worlds%]");
	}
	
	private Variable<World> worlds = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		worlds = (Variable<World>) vars[0];
	}
	
	@Override
	protected Time[] getAll(final Event e) {
		return worlds.getArray(e, Time.class, new Getter<Time, World>() {
			@Override
			public Time get(final World w) {
				return new Time((int) w.getTime());
			}
		});
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return DefaultChangers.timeChanger.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		DefaultChangers.timeChanger.change(e, worlds, delta, mode);
	}
	
	@Override
	public Class<Time> getReturnType() {
		return Time.class;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (e == null)
			return "time in " + worlds.getDebugMessage(e);
		return Skript.getDebugMessage(getAll(e));
	}
	
	@Override
	public String toString() {
		return "the time in " + worlds;
	}
	
	@Override
	public boolean isSingle() {
		return worlds.isSingle();
	}
	
}