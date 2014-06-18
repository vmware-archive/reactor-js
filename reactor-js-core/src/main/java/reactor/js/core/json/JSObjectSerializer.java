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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import jdk.nashorn.api.scripting.JSObject;

import java.io.IOException;

/**
 * @author Jon Brisbin
 */
class JSObjectSerializer extends JsonSerializer<JSObject> {
	@Override
	public void serialize(JSObject value,
	                      JsonGenerator jgen,
	                      SerializerProvider provider) throws IOException,
	                                                          JsonProcessingException {
		if (value.isArray()) {
		    jgen.writeStartArray();
			int i = -1;
			for (; ; ) {
				if (value.hasSlot(++i)) {
					jgen.writeObject(value.getSlot(i));
				} else {
					break;
				}
			}
			jgen.writeEndArray();
		} else {
		    jgen.writeStartObject();
			for (String key : value.keySet()) {
			    jgen.writeObjectField(key, value.getMember(key));
			}
			jgen.writeEndObject();
		}
	}
}
