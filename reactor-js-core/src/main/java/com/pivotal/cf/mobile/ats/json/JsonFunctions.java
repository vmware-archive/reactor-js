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

package com.pivotal.cf.mobile.ats.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;


import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Will Tran <wtran@pivotallabs.com>
 */
public class JsonFunctions {

	private static final ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	
		SimpleModule mod = new SimpleModule();
		mod.addSerializer(new UndefinedSerializer());
		mod.addSerializer(new ScriptObjectSerializer());
		mod.addSerializer(new ScriptObjectMirrorSerializer());
		mapper.registerModule(mod);
	}

	public static ObjectMapper getObjectMapper() {
	    return mapper;
	}

	public static Object parse(String json) throws IOException {
		return convert(mapper.readValue(json, Object.class));
	}
	
	// recursively replace Collections and Maps with native JavaScript arrays and objects.
	public static Object convert(Object o) {
	    if (o instanceof Collection<?>) {
	        List<?> original = (List<?>) o;
	        Object[] array = original.toArray();
	        for (int i = 0; i < array.length; i++) {
	            array[i] = convert(array[i]);
	        }
	        return jdk.nashorn.internal.objects.Global.allocate(array);
	    } else if (o instanceof Map<?,?>) {
	        jdk.nashorn.internal.scripts.JO jo = new jdk.nashorn.internal.scripts.JO(
                    jdk.nashorn.internal.runtime.PropertyMap.newMap());
	        ((Map<?,?>)o).forEach((k,v) -> jo.put(k, convert(v), false));
            return jo;
	    } else {
	        return o;
	    }
	}

	public static String stringify(Object obj) throws JsonProcessingException {
		return mapper.writeValueAsString(obj);
	}

}
