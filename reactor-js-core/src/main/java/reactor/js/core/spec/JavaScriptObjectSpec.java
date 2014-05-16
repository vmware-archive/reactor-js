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

package reactor.js.core.spec;

import reactor.function.Function;
import reactor.function.Supplier;
import reactor.js.core.JavaScriptObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jon Brisbin
 */
public class JavaScriptObjectSpec implements Supplier<JavaScriptObject> {

	private final Map<String, Object>      properties        = new HashMap<>();
	private final Map<String, Supplier<?>> propertySuppliers = new HashMap<>();

	private Object parent;

	public static JavaScriptObjectSpec jsObj() {
		return new JavaScriptObjectSpec();
	}

	public <T> JavaScriptObjectSpec with(String name, T val) {
		properties.put(name, val);
		return this;
	}

	public <T> JavaScriptObjectSpec with(String name, Supplier<T> supplier) {
		propertySuppliers.put(name, supplier);
		return this;
	}

	public <T, V> JavaScriptObjectSpec with(Function<T, V> fn) {
		properties.put("apply", fn);
		return this;
	}

	public JavaScriptObjectSpec parent(Object parent) {
		this.parent = parent;
		return this;
	}

	@Override
	public JavaScriptObject get() {
		return new JavaScriptObject(properties, propertySuppliers, parent);
	}

}
