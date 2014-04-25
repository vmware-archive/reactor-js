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

import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.list.mutable.SynchronizedMutableList;
import jdk.nashorn.api.scripting.AbstractJSObject;

import java.util.Collection;
import java.util.List;

/**
 * @author Jon Brisbin
 */
public class JavaScriptArray extends AbstractJSObject {

	private final List elements;

	public JavaScriptArray() {
		this(SynchronizedMutableList.of(FastList.newList()));
	}

	public JavaScriptArray(List<?> elements) {
		this.elements = elements;
	}

	public static <T> JavaScriptArray from(List<T> elements) {
		return new JavaScriptArray(elements);
	}

	public List getElements() {
		return elements;
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

}
