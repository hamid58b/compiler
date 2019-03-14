/*
 * Copyright 2017, Anthony Urso, Hridesh Rajan, Robert Dyer, 
 *                 Iowa State University of Science and Technology
 *                 and Bowling Green State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link BoaType} representing a data structure with named members of
 * arbitrary type.
 * 
 * @author anthonyu
 * @author rdyer
 */
public class BoaTuple extends BoaType {
	protected final List<BoaType> members;
	protected final Map<String, Integer> names;

	public BoaTuple() {
		members = new ArrayList<BoaType>();
		names = new HashMap<String, Integer>();
	}

	public BoaTuple(final List<BoaType> members) {
		this.members = members;
		this.names = new HashMap<String, Integer>();
		for (int i = 0; i < this.members.size(); i++) {
			BoaType t = this.members.get(i);
			if (t instanceof BoaName) {
				this.names.put(((BoaName) t).getId(), i);
				this.members.set(i, ((BoaName) t).getType());
			}
			this.names.put("_" + (i + 1), i);
		}
	}

	protected BoaTuple(final List<BoaType> members, final Map<String, Integer> names) {
		this.members = members;
		this.names = names;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		// if that is a function, check the return type
		if (that instanceof BoaFunction)
			return this.assigns(((BoaFunction) that).getType());

		// if that is a component, check the type
		if (that instanceof BoaName)
			return this.assigns(((BoaName) that).getType());

		if (that instanceof BoaArray) {
			// if this is an empty tuple, dont allow assigning an array to it
			if (this.members.size() == 0 && this.names.size() == 0)
				return false;

			BoaType type = ((BoaArray) that).getType();
			if (type instanceof BoaName)
				type = ((BoaName) type).getType();
			for (final BoaType t : this.members)
				if (!t.assigns(type))
					return false;
			return true;
		}

		if (!(that instanceof BoaTuple))
			return false;

		final BoaTuple other = (BoaTuple)that;
		// if the other is an empty tuple, we always allow it
		if (other.members.size() == 0 && other.names.size() == 0)
			return true;
		if (this.members.size() != other.members.size())
			return false;
		for (int i = 0; i < this.members.size(); i++)
			if (this.members.get(i).getClass() != other.members.get(i).getClass())
				return false;

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		return this.assigns(that);
	}

	/**
	 * 
	 * @param member
	 *            A {@link String} containing the name of the member
	 * 
	 * @return true if a member exists in this tuple with the given name
	 */
	public boolean hasMember(final String member) {
		return this.names.containsKey(member);
	}

	/**
	 * Return the type of the member identified by a given index.
	 * 
	 * @param index
	 *            An int containing the index of the member
	 * 
	 * @return A {@link BoaType} representing the type of the member
	 * 
	 */
	public BoaType getMember(final int index) {
		return this.members.get(index - 1);
	}

	/**
	 * Return the type of the member identified by a given name.
	 * 
	 * @param member
	 *            A {@link String} containing the name of the member
	 * 
	 * @return A {@link BoaType} representing the type of the member
	 * 
	 */
	public BoaType getMember(final String member) {
		return this.members.get(this.names.get(member));
	}

	public int getMemberIndex(final String member) {
		return this.names.get(member) + 1;
	}

	public String getMemberName(final String member) {
		final BoaType t = this.members.get(this.names.get(member));
		if (t instanceof BoaName)
			return ((BoaName)t).getId();
		return member;
	}

	public List<BoaType> getTypes() {
		return this.members;
	}

	@Override
	public String toJavaType() {
		String s = "";

		for (final BoaType t : this.members)
			s += "_" + cleanType(t.toJavaType());

		return shortenedType(s, "BoaTup");
	}

	private int hash = 0;
	private boolean hashed = false;

	@Override
	public int hashCode() {
		if (!hashed) {
			final int prime = 31;
			hash = super.hashCode();
			hashed = true;
			hash = prime * hash + (this.members == null ? 0 : this.members.hashCode());
		}
		return hash;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final BoaTuple other = (BoaTuple) obj;
		if (this.members == null && other.members != null)
			return false;
		return this.members.equals(other.members);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "tuple " + this.members.toString();
	}
}
