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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jayway.jsonpath.spi.JsonProvider;
import com.jayway.jsonpath.spi.JsonProviderFactory;
import jdk.nashorn.api.scripting.JSObject;

import java.io.IOException;

/**
 * @author Jon Brisbin
 */
public class JsonFunctions {

	private static final ObjectMapper mapper;
	private static final JsonProvider jsonProvider = new NashornJacksonJsonPathProvider();

	static {
		JsonProviderFactory.setProvider(jsonProvider);
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		SimpleModule mod = new SimpleModule();
		mod.addSerializer(JSObject.class, new JSObjectSerializer());
		mod.addDeserializer(JSObject.class, new JSObjectDeserializer());
		mapper.registerModule(mod);
	}

	public static JSObject path(String jsonPath) {
		return new JsonPathFunction(jsonProvider, jsonPath);
	}

	public static JSObject parse(String json) throws IOException {
		return mapper.readValue(json, JSObject.class);
	}

	public static String stringify(Object obj) throws JsonProcessingException {
		return mapper.writeValueAsString(obj);
	}

}
