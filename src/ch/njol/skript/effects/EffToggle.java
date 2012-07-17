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

package ch.njol.skript.effects;

import org.bukkit.block.Block;
import org.bukkit.event.Event;

import ch.njol.skript.api.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * TODO
 * 
 * @author Peter Güttinger
 * 
 */
public class EffToggle extends Effect {
	
	static {
//		Skript.registerEffect(EffToggle.class, "toggle %blocks%");
	}
	
	private Expression<Block> blocks;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult parseResult) {
		blocks = (Expression<Block>) vars[0];
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "toggle " + blocks.toString(e, debug);
	}
	
	@Override
	protected void execute(final Event e) {
		for (final Block b : blocks.getArray(e)) {
			switch (b.getType()) {
				case LEVER:
					
				default:
					continue;
			}
		}
	}
	
}