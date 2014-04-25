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

import com.gs.collections.impl.map.mutable.SynchronizedMutableMap;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import jdk.nashorn.api.scripting.AbstractJSObject;
import reactor.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jon Brisbin
 */
public class JavaScriptObject extends AbstractJSObject {

	private final Map<String, Object> properties;

	public JavaScriptObject() {
		this(SynchronizedMutableMap.of(new UnifiedMap<String, Object>()));
	}

	public JavaScriptObject(Map<String, Object> properties) {
		Assert.notNull(properties, "Properties map cannot be null");
		this.properties = properties;
	}

	public static JavaScriptObject from(Map<String, Object> properties) {
		return new JavaScriptObject(properties);
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public Object newObject(Object... args) {
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getMember(String name) {
		Object obj = properties.get(name);
		if (Map.class.isInstance(obj)) {
			return JavaScriptObject.from((Map<String, Object>) obj);
		} else if (List.class.isInstance(obj)) {
			return JavaScriptArray.from((List) obj);
		} else {
			return obj;
		}
	}

	@Override
	public boolean hasMember(String name) {
		return properties.containsKey(name);
	}

	@Override
	public void removeMember(String name) {
		properties.remove(name);
	}

	@Override
	public void setMember(String name, Object value) {
		properties.put(name, value);
	}

	@Override
	public Set<String> keySet() {
		return properties.keySet();
	}

	@Override
	public Collection<Object> values() {
		return properties.values();
	}

	@Override
	public boolean isInstance(Object instance) {
		return JavaScriptObject.class.isInstance(instance);
	}

	@Override
	public String getClassName() {
		return "JavaScriptObject";
	}

}
