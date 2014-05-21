/*
 * Copyright (c) 2011-2014 GoPivotal, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package reactor.js.core;

import jdk.nashorn.api.scripting.AbstractJSObject;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Jon Brisbin
 */
public class JavaScriptArray extends AbstractJSObject implements Cloneable {

	private final List<Object> elements;

	public JavaScriptArray() {
		this(null);
	}

	public JavaScriptArray(List<?> elements) {
		this.elements = new CopyOnWriteArrayList<>((null != elements ? elements : Collections.emptyList()));
	}

	public int size() {
		return elements.size();
	}

	public Object newObject(Object... args) {
		return this;
	}

	@Override
	public Object getSlot(int index) {
		return elements.get(index);
	}

	@Override
	public boolean hasSlot(int slot) {
		return elements.size() > slot && null != elements.get(slot);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setSlot(int index, Object value) {
		elements.set(index, value);
	}

	@Override
	public boolean isInstance(Object instance) {
		return JavaScriptArray.class.isInstance(instance);
	}

	@Override
	public String getClassName() {
		return "JavaScriptArray";
	}

	@Override
	public boolean isArray() {
		return true;
	}

	@Override
	public Collection<Object> values() {
		return elements;
	}

	public JavaScriptArray copyOf() {
		return new JavaScriptArray(elements);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new JavaScriptArray(elements);
	}

	@Override
	public String toString() {
		return "JavaScriptArray{" +
				"elements=" + elements +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof JavaScriptArray)) return false;

		JavaScriptArray other = (JavaScriptArray) o;

		return !(elements != null ? !elements.equals(other.elements) : other.elements != null);
	}

	@Override
	public int hashCode() {
		return elements != null ? elements.hashCode() : 0;
	}

}
