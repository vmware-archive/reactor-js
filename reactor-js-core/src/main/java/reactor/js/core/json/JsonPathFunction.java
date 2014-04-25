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

package reactor.js.core.json;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.JsonProvider;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;

import java.util.Collection;
import java.util.Set;

/**
 * @author Jon Brisbin
 */
public class JsonPathFunction extends AbstractJSObject {

	private final Configuration config;
	private final JsonPath      path;

	public JsonPathFunction(JsonProvider provider, String path) {
		this.config = Configuration.builder()
		                           .jsonProvider(provider)
		                           .build();
		this.path = JsonPath.compile(path);
	}

	@Override
	public Object newObject(Object... args) {
		return this;
	}

	@Override
	public Object getMember(String name) {
		if ("apply".equals(name)) {
			return this;
		} else {
			return null;
		}
	}

	@Override
	public Object getSlot(int index) {
		return super.getSlot(index);
	}

	@Override
	public boolean hasMember(String name) {
		return super.hasMember(name);
	}

	@Override
	public boolean hasSlot(int slot) {
		return super.hasSlot(slot);
	}

	@Override
	public void removeMember(String name) {
		super.removeMember(name);
	}

	@Override
	public void setMember(String name, Object value) {
		super.setMember(name, value);
	}

	@Override
	public void setSlot(int index, Object value) {
		super.setSlot(index, value);
	}

	@Override
	public Set<String> keySet() {
		return super.keySet();
	}

	@Override
	public Collection<Object> values() {
		return super.values();
	}

	@Override
	public boolean isInstance(Object instance) {
		return super.isInstance(instance);
	}

	@Override
	public boolean isInstanceOf(Object clazz) {
		return super.isInstanceOf(clazz);
	}

	@Override
	public boolean isFunction() {
		return true;
	}

	@Override
	public boolean isStrictFunction() {
		return true;
	}

	@Override
	public Object call(Object thiz, Object... args) {
		if (args.length == 1 && args[0] instanceof JSObject) {
			return path.read(args[0], config);
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return path.getPath();
	}

}
