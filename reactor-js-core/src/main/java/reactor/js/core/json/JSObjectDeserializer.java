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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import jdk.nashorn.api.scripting.JSObject;
import reactor.js.core.JavaScriptArray;
import reactor.js.core.JavaScriptObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Jon Brisbin
 */
class JSObjectDeserializer extends JsonDeserializer<JSObject> {
	@SuppressWarnings("unchecked")
	@Override
	public JSObject deserialize(JsonParser jp,
	                            DeserializationContext ctxt) throws IOException,
	                                                                JsonProcessingException {
		Object obj = jp.readValueAs(Object.class);
		if (Map.class.isInstance(obj)) {
			return JavaScriptObject.from((Map) obj);
		} else if (List.class.isInstance(obj)) {
			return JavaScriptArray.from((List) obj);
		} else {
			throw new JsonMappingException("Cannot convert value to a valid JSON object", jp.getCurrentLocation());
		}
	}
}
