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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.spi.MappingProvider;
import com.jayway.jsonpath.spi.Mode;
import com.jayway.jsonpath.spi.impl.AbstractJsonProvider;
import jdk.nashorn.api.scripting.JSObject;
import reactor.js.core.JavaScriptArray;
import reactor.js.core.JavaScriptObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Jon Brisbin
 */
public class NashornJacksonJsonPathProvider extends AbstractJsonProvider implements MappingProvider {

	private final ObjectMapper mapper;

	public NashornJacksonJsonPathProvider() {
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		SimpleModule mod = new SimpleModule();
		mod.addSerializer(JSObject.class, new JSObjectSerializer());
		mod.addDeserializer(JSObject.class, new JSObjectDeserializer());
		mapper.registerModule(mod);
	}

	@Override
	public Mode getMode() {
		return Mode.STRICT;
	}

	@Override
	public Object parse(String json) throws InvalidJsonException {
		try {
			return mapper.readValue(json, Object.class);
		} catch (IOException e) {
			throw new InvalidJsonException(e.getMessage(), e);
		}
	}

	@Override
	public Object parse(Reader jsonReader) throws InvalidJsonException {
		try {
			return mapper.readValue(jsonReader, Object.class);
		} catch (IOException e) {
			throw new InvalidJsonException(e.getMessage(), e);
		}
	}

	@Override
	public Object parse(InputStream jsonStream) throws InvalidJsonException {
		try {
			return mapper.readValue(jsonStream, Object.class);
		} catch (IOException e) {
			throw new InvalidJsonException(e.getMessage(), e);
		}
	}

	@Override
	public String toJson(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	@Override
	public Object createMap() {
		return UnifiedMap.newMap();
	}

	@Override
	public Iterable createArray() {
		return FastList.newList();
	}

	@Override
	public <T> T convertValue(Object fromValue, Class<T> toValueType) throws IllegalArgumentException {
		return mapper.convertValue(fromValue, toValueType);
	}

	@Override
	public <T extends Collection<E>, E> T convertValue(Object fromValue,
	                                                   Class<T> collectionType,
	                                                   Class<E> elementType) throws IllegalArgumentException {
		JavaType collType = mapper.getTypeFactory().constructCollectionType(collectionType, elementType);
		return mapper.convertValue(fromValue, collType);
	}

	@Override
	public boolean isContainer(Object obj) {
		return JSObject.class.isInstance(obj);
	}

	@Override
	public boolean isMap(Object obj) {
		if (Map.class.isInstance(obj)) {
			return true;
		} else if (JSObject.class.isInstance(obj)) {
			return !((JSObject) obj).isArray();
		} else {
			return false;
		}
	}

	@Override
	public boolean isArray(Object obj) {
		if (JSObject.class.isInstance(obj)) {
			return ((JSObject) obj).isArray();
		} else {
			return false;
		}
	}

	@Override
	public Object getProperty(Object obj, Object key) {
		if (Map.class.isInstance(obj)) {
			return ((Map) obj).get(key);
		} else if (JSObject.class.isInstance(obj)) {
			return ((JSObject) obj).getMember(key.toString());
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setProperty(Object obj, Object key, Object value) {
		if (Map.class.isInstance(obj)) {
			((Map) obj).put(key, value);
		} else if (JSObject.class.isInstance(obj)) {
			((JSObject) obj).setMember(key.toString(), value);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<String> getPropertyKeys(Object obj) {
		if (Map.class.isInstance(obj)) {
			return FastList.newList(((Map) obj).keySet());
		} else if (JSObject.class.isInstance(obj)) {
			return ((JSObject) obj).keySet();
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public int length(Object obj) {
		return (JavaScriptArray.class.isInstance(obj)
				? ((JavaScriptArray) obj).getElements().size()
				: 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<Object> toIterable(Object obj) {
		return (JavaScriptArray.class.isInstance(obj)
				? ((JavaScriptArray) obj).getElements()
				: Collections.emptyList());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object clone(Object obj) {
		if (JavaScriptArray.class.isInstance(obj)) {
			return JavaScriptArray.from(((JavaScriptArray) obj).getElements());
		} else if (JavaScriptObject.class.isInstance(obj)) {
			return JavaScriptObject.from(UnifiedMap.newMap(((JavaScriptObject) obj).getProperties()));
		} else {
			throw new IllegalStateException("Clone not supported for " + obj.getClass().getName());
		}
	}

}
