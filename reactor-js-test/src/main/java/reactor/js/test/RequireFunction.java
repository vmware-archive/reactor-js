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

package reactor.js.test;

import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.internal.objects.NativeArray;
import jdk.nashorn.internal.runtime.Undefined;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Jon Brisbin
 */
class RequireFunction extends AbstractJSObject {

	private final Map<String, Object> properties = new HashMap<>();

	private final ScriptEngine engine;
	private final Bindings     bindings;

	public RequireFunction(ScriptEngine engine, Bindings bindings) {
		this.engine = engine;
		this.bindings = bindings;
	}

	@Override
	public Object call(Object thiz, Object... args) {
		if (args.length > 1 || args.length == 0) {
			throw new IllegalArgumentException("USAGE: require(moduleId);");
		}

		String moduleId = (String) args[0];
		if (!moduleId.endsWith(".js")) {
			moduleId = moduleId + ".js";
		}

		if (moduleId.startsWith("classpath:")) {
			try {
				return engine.eval("load(\"" + moduleId + "\");", bindings);
			} catch (ScriptException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}

		Path modulePath = null;

		if (hasMember("paths")) {
			NativeArray paths = (NativeArray) getMember("paths");
			int len = ((Long) paths.getLength()).intValue();
			for (int i = 0; i < len; i++) {
				if (null != (modulePath = findPath(paths.get(i).toString(), moduleId))) {
					break;
				}
			}
		} else {
			modulePath = findPath(".", moduleId);
		}

		if (null == modulePath) {
			throw new IllegalArgumentException("Cannot find module \"" + args[0] + "\"");
		}

		try {
			return engine.eval("load(\"" + modulePath.toAbsolutePath().toString() + "\");", bindings);
		} catch (ScriptException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public boolean isFunction() {
		return true;
	}

	@Override
	public boolean isStrictFunction() {
		return isFunction();
	}

	@Override
	public void setMember(String name, Object value) {
		properties.put(name, value);
	}

	@Override
	public boolean hasMember(String name) {
		return properties.containsKey(name);
	}

	@Override
	public Object getMember(String name) {
		return properties.containsKey(name) ? properties.get(name) : Undefined.getUndefined();
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
	public String getClassName() {
		return "RequireFunction";
	}

	private Path findPath(String root, String moduleId) {
		Path rootPath = Paths.get(root, moduleId),
				nodePath = Paths.get(root, "node_modules", moduleId),
				jsPath = Paths.get(root, "js_modules", moduleId);

		if (Files.exists(rootPath)) {
			return Paths.get(root, moduleId);
		} else if (Files.exists(nodePath)) {
			return nodePath;
		} else if (Files.exists(jsPath)) {
			return jsPath;
		}

		return null;
	}

}
