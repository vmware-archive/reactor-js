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

package reactor.js.core.module;

import com.gs.collections.impl.map.mutable.UnifiedMap;
import reactor.js.core.JavaScriptObject;

import java.net.URI;
import java.util.Arrays;

/**
 * @author Jon Brisbin
 */
public class JavaScriptModule extends JavaScriptObject {

	public JavaScriptModule(String id, URI uri) {
		super(UnifiedMap.newWithKeysValues("id", id, "uri", uri.toString()), null, Arrays.asList("id", "uri"), null);
	}

}
