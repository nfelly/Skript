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

package ch.njol.skript.api;

import ch.njol.skript.Skript;
import ch.njol.skript.data.DefaultComparators;

/**
 * Used to compare two objects of a different or the same type.
 * 
 * @author Peter Güttinger
 * 
 * @param <T1> ,
 * @param <T2> the types to compare
 * 
 * @see Skript#registerComparator(Class, Class, Comparator)
 * @see DefaultComparators
 */
public interface Comparator<T1, T2> {
	
	/**
	 * represents a relation between two objects.
	 */
	public static enum Relation {
		EQUAL, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, SMALLER, SMALLER_OR_EQUAL;
		
		public static Relation get(final boolean b) {
			return b ? Relation.EQUAL : Relation.NOT_EQUAL;
		}
		
		public static Relation get(final int i) {
			return i == 0 ? Relation.EQUAL : i > 0 ? Relation.GREATER : Relation.SMALLER;
		}
		
		public static Relation get(final double d) {
			return d == 0 ? Relation.EQUAL : d > 0 ? Relation.GREATER : Relation.SMALLER;
		}
		
		/**
		 * Test whether this relation is fulfilled if another is, e.g. EQUAL.is(GREATER_OR_EQUAL) returns true.
		 * 
		 * @param other
		 * @return
		 */
		public boolean is(final Relation other) {
			if (other == this)
				return true;
			switch (this) {
				case EQUAL:
					return other == SMALLER_OR_EQUAL || other == GREATER_OR_EQUAL;
				case NOT_EQUAL:
					return other == SMALLER || other == GREATER;
				case GREATER:
					return other == GREATER_OR_EQUAL;
				case GREATER_OR_EQUAL:
					return other == EQUAL || other == GREATER;
				case SMALLER:
					return other == SMALLER_OR_EQUAL;
				case SMALLER_OR_EQUAL:
					return other == EQUAL || other == SMALLER;
			}
			return false;
		}
		
		/**
		 * Returns this relation's string representation, which is similar to "equal to" or "greater than".
		 */
		@Override
		public String toString() {
			switch (this) {
				case EQUAL:
					return "equal to";
				case NOT_EQUAL:
					return "not equal to";
				case GREATER:
					return "greater than";
				case GREATER_OR_EQUAL:
					return "greater than or equal to";
				case SMALLER:
					return "smaller than";
				case SMALLER_OR_EQUAL:
					return "smaller than or equal to";
			}
			throw new RuntimeException();
		}
		
		/**
		 * Gets the inverse of this relation, i.e if this relation fulfills <code>X rel Y</code>, then the returned relation fullfills <code>!(X rel Y)</code>.
		 * 
		 * @return
		 */
		public Relation getInverse() {
			switch (this) {
				case EQUAL:
					return NOT_EQUAL;
				case NOT_EQUAL:
					return EQUAL;
				case GREATER:
					return SMALLER_OR_EQUAL;
				case GREATER_OR_EQUAL:
					return SMALLER;
				case SMALLER:
					return GREATER_OR_EQUAL;
				case SMALLER_OR_EQUAL:
					return GREATER;
			}
			throw new RuntimeException();
		}
		
		/**
		 * Gets the relation which has switched arguments, i.e. if this relation fulfills <code>X rel Y</code>, then the returned relation fullfills <code>Y rel X</code>.
		 * 
		 * @return
		 */
		public Relation getSwitched() {
			switch (this) {
				case GREATER:
					return SMALLER;
				case GREATER_OR_EQUAL:
					return SMALLER_OR_EQUAL;
				case SMALLER:
					return GREATER;
				case SMALLER_OR_EQUAL:
					return GREATER_OR_EQUAL;
				default:
					return this;
			}
		}
		
		public boolean isEqualOrInverse() {
			return this == Relation.EQUAL || this == Relation.NOT_EQUAL;
		}
	}
	
	/**
	 * holds information a about a comparator.
	 * 
	 * @param <T1> see {@link Comparator}
	 * @param <T2> dito
	 */
	public static class ComparatorInfo<T1, T2> {
		
		public Class<T1> c1;
		public Class<T2> c2;
		public Comparator<T1, T2> c;
		
		public ComparatorInfo(final Class<T1> c1, final Class<T2> c2, final Comparator<T1, T2> c) {
			this.c1 = c1;
			this.c2 = c2;
			this.c = c;
		}
		
		public Class<?> getType(final int i) {
			return i == 0 ? c1 : c2;
		}
		
	}
	
	Comparator<?, ?> equalsComparator = new Comparator<Object, Object>() {
		@Override
		public ch.njol.skript.api.Comparator.Relation compare(final Object o1, final Object o2) {
			if (o1 == null || o2 == null)
				return Relation.get(o1 == o2);
			return Relation.get(o1.equals(o2));
		}
		
		@Override
		public boolean supportsOrdering() {
			return false;
		}
	};
	
	/**
	 * Compares the given objects. Must be null-safe for both arguments.
	 * 
	 * @param o1
	 * @param o2
	 * @return the relation of the obects. GREATER/SMALLER means the first parameter is greater/smaller. NOT_EQUAL is used for values which can't be compared by value.
	 */
	public Relation compare(T1 o1, T2 o2);
	
	/**
	 * 
	 * @return whether this comparator supports ordering of elements or not
	 */
	public boolean supportsOrdering();
	
}