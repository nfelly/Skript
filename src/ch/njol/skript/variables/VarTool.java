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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.api.Getter;
import ch.njol.skript.data.DefaultChangers;
import ch.njol.skript.lang.ExprParser.ParseResult;
import ch.njol.skript.lang.SimpleVariable;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.Slot;

/**
 * 
 * @author Peter Güttinger
 * 
 */
public class VarTool extends SimpleVariable<Slot> {
	
	static {
		Skript.registerVariable(VarTool.class, Slot.class, "(tool|held item) [of %players%]", "%player%'[s] (tool|held item)");
	}
	
	private Variable<Player> players;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(final Variable<?>[] vars, final int matchedPattern, final ParseResult parser) {
		players = (Variable<Player>) vars[0];
	}
	
	@Override
	protected Slot[] getAll(final Event e) {
		return players.getArray(e, Slot.class, new Getter<Slot, Player>() {
			@Override
			public Slot get(final Player p) {
				return new Slot(p.getInventory(), p.getInventory().getHeldItemSlot()) {
					@Override
					public void setItem(final ItemStack item) {
						p.setItemInHand(item);
					}
					
					@Override
					public ItemStack getItem() {
						return p.getItemInHand();
					}
					
					@Override
					public String getDebugMessage(final Event e) {
						return "tool of " + p.getName();
					}
				};
			}
		});
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return DefaultChangers.slotChanger.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Variable<?> delta, final ChangeMode mode) {
		DefaultChangers.slotChanger.change(e, this, delta, mode);
	}
	
	@Override
	public Class<Slot> getReturnType() {
		return Slot.class;
	}
	
	@Override
	public String getDebugMessage(final Event e) {
		if (e == null)
			return "tool of " + players.getDebugMessage(e);
		return Skript.getDebugMessage(getSingle(e));
	}
	
	@Override
	public String toString() {
		return "the tool of " + players;
	}
	
	@Override
	public boolean isSingle() {
		return players.isSingle();
	}
	
}