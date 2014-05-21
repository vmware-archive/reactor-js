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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.gs.collections.api.list.ImmutableList;
import com.gs.collections.api.map.MutableMap;
import com.gs.collections.api.set.MutableSet;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.SynchronizedMutableMap;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import com.gs.collections.impl.set.mutable.UnifiedSet;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.ScriptObject;
import jdk.nashorn.internal.runtime.ScriptRuntime;
import org.jetbrains.annotations.NotNull;
import reactor.alloc.Recyclable;
import reactor.function.Supplier;

import javax.script.Bindings;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

/**
 * @author Jon Brisbin
 */
public class JavaScriptObject extends AbstractJSObject implements Bindings, Recyclable, Cloneable {

	private static final MutableMap<Class<?>, Function<Object, Object>> FIELD_ACCESSORS
			= SynchronizedMutableMap.of(UnifiedMap.newMap());
	private static final Supplier<?>                                    NULL_SUPPLIER
			= () -> null;

	private enum ParentType {
		JSOBJECT,
		SCRIPTOBJECT,
		MAP,
		BEAN;

		public Object get(Object obj, String key) {
			switch (this) {
				case JSOBJECT:
					return ((JSObject) obj).getMember(key);
				case SCRIPTOBJECT:
					return ((ScriptObject) obj).get(key);
				case MAP:
					return ((Map) obj).get(key);
				case BEAN:
					return FIELD_ACCESSORS.getIfAbsentPut(obj.getClass(), () -> new FieldAccessor(obj, key)).apply(key);
				default:
					return null;
			}
		}

		public static ParentType of(Object obj) {
			if (obj instanceof JSObject) {
				return ParentType.JSOBJECT;
			} else if (obj instanceof ScriptObject) {
				return ParentType.SCRIPTOBJECT;
			} else if (obj instanceof Map) {
				return ParentType.MAP;
			} else {
				return ParentType.BEAN;
			}
		}
	}

	private final MutableMap<String, Object>      properties;
	private final MutableMap<String, Supplier<?>> propertySuppliers;
	private final ImmutableList<String>           readOnlyProperties;

	private volatile Object     parent;
	private volatile ParentType parentType;

	public JavaScriptObject() {
		this(null, null, null, null);
	}

	public JavaScriptObject(Map<String, Object> properties,
	                        Map<String, Supplier<?>> propertySuppliers,
	                        List<String> readOnlyProperties,
	                        Object parent) {
		if (null == properties) {
			properties = Collections.emptyMap();
		}
		if (null == propertySuppliers) {
			propertySuppliers = Collections.emptyMap();
		}
		if (null == readOnlyProperties) {
			readOnlyProperties = Collections.emptyList();
		}
		this.properties = UnifiedMap.newMap(properties);
		this.propertySuppliers = UnifiedMap.newMap(propertySuppliers);
		this.readOnlyProperties = FastList.newList(readOnlyProperties).toImmutable();
		setParent(parent);
	}

	public void setParent(Object parent) {
		this.parent = parent;
		this.parentType = ParentType.of(parent);
	}

	@Override
	public Object call(Object thiz, Object... args) {
		if (!hasProperty("apply")) {
			throw new NoSuchMethodError(this + " does not have a property 'apply' so cannot be invoked.");
		}
		Object apply = getProperty("apply");
		if (apply instanceof ScriptFunction) {
			return ScriptRuntime.apply((ScriptFunction) apply, thiz, args);
		} else if (apply instanceof JSObject) {
			return ((JSObject) apply).call(thiz, args);
		} else {
			throw new IllegalArgumentException("Cannot invoke an instance of " + apply);
		}
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public Object newObject(Object... args) {
		return new JavaScriptObject(properties, propertySuppliers, readOnlyProperties.castToList(), this);
	}

	public <T> JavaScriptObject supply(String name, Supplier<T> supplier) {
		propertySuppliers.put(name, supplier);
		return this;
	}

	@JsonAnySetter
	@Override
	public Object put(String name, Object value) {
		if (readOnlyProperties.contains(name)) {
			throw new IllegalArgumentException("Property '" + name + "' is read-only");
		}
		return properties.put(name, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> toMerge) {
		for (Entry<? extends String, ? extends Object> entry : toMerge.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public boolean containsKey(Object key) {
		return hasProperty(key);
	}

	@JsonAnyGetter
	@Override
	public Object get(Object key) {
		return getMember(key.toString());
	}

	@Override
	public Object remove(Object key) {
		if (readOnlyProperties.contains(key)) {
			throw new IllegalArgumentException("Property '" + key + "' is read-only");
		}
		return properties.remove(key);
	}

	@Override
	public int size() {
		return keySet().size();
	}

	@Override
	public boolean isEmpty() {
		return properties.isEmpty() && propertySuppliers.isEmpty();
	}

	@Override
	public boolean containsValue(Object value) {
		return properties.containsValue(value);
	}

	@Override
	public void clear() {
		properties.clear();
	}

	@NotNull
	@Override
	public Set<Entry<String, Object>> entrySet() {
		MutableSet<Entry<String, Object>> entries = UnifiedSet.newSet();
		entries.addAll(properties.entrySet());
		propertySuppliers.forEachKeyValue((key, supplier) -> {
			if (!properties.containsKey(key)) {
				entries.add(new AbstractMap.SimpleImmutableEntry<>(key, supplier.get()));
			}
		});
		return entries.asUnmodifiable();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getMember(String name) {
		Object obj = properties.computeIfAbsent(name,
		                                        s -> propertySuppliers.containsKey(s)
				                                        ? propertySuppliers.get(s).get()
				                                        : null
		);
		if (Map.class.isInstance(obj)) {
			return new JavaScriptObject((Map<String, Object>) obj, null, null, parent);
		} else if (List.class.isInstance(obj)) {
			return new JavaScriptArray((List) obj);
		} else {
			return obj;
		}
	}

	@Override
	public boolean hasMember(String name) {
		return hasProperty(name);
	}

	@Override
	public void removeMember(String name) {
		remove(name);
	}

	@Override
	public void setMember(String name, Object value) {
		put(name, value);
	}

	@Override
	public Set<String> keySet() {
		MutableSet<String> keys = UnifiedSet.newSet();
		properties.forEachKey(keys::add);
		propertySuppliers.forEachKey(keys::add);
		return keys.asUnmodifiable();
	}

	@Override
	public Collection<Object> values() {
		return ((MutableSet<Entry<String, Object>>) entrySet()).collect(Entry<String, Object>::getValue);
	}

	@Override
	public boolean isInstance(Object instance) {
		return JavaScriptObject.class.isInstance(instance);
	}

	@Override
	public boolean isFunction() {
		return hasMember("apply");
	}

	@Override
	public boolean isStrictFunction() {
		return false;
	}

	@Override
	public String getClassName() {
		return "JavaScriptObject";
	}

	private boolean hasProperty(Object prop) {
		return null != getProperty(prop);
	}

	private Object getProperty(Object prop) {
		String key = prop.toString();

		if (properties.containsKey(key) || propertySuppliers.containsKey(key)) {
			return properties.getIfAbsentPut(key, () -> propertySuppliers.getIfAbsentValue(key, NULL_SUPPLIER).get());
		} else if (null != parent) {
			return parentType.get(parent, key);
		} else {
			return null;
		}
	}

	@Override
	public void recycle() {
		clear();
	}

	public JavaScriptObject copyOf() {
		try {
			return (JavaScriptObject) clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new JavaScriptObject(properties, propertySuppliers, readOnlyProperties.castToList(), parent);
	}

	@Override
	public String toString() {
		return "JavaScriptObject{" +
				"properties=" + properties +
				", propertySuppliers=" + propertySuppliers +
				", readOnlyProperties=" + readOnlyProperties +
				", parent=" + parent +
				'}';
	}

	@Override
	public boolean equals(Object obj) {
		if (!JavaScriptObject.class.isInstance(obj)) {
			return false;
		}
		JavaScriptObject other = (JavaScriptObject) obj;

		return other.properties.equals(properties)
				&& other.propertySuppliers.equals(propertySuppliers)
				&& other.readOnlyProperties.equals(readOnlyProperties)
				&& other.parent == parent;
	}

	private static class FieldAccessor implements Function<Object, Object> {
		private Field field;

		private FieldAccessor(Object obj, String field) {
			try {
				this.field = obj.getClass().getDeclaredField(field);
				this.field.setAccessible(true);
			} catch (NoSuchFieldException e) {
			}
		}

		@Override
		public Object apply(Object o) {
			try {
				return (null != field ? field.get(o) : null);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
	}

}
