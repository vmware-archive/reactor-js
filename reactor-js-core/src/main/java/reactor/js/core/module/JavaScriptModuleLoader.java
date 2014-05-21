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

import jdk.nashorn.api.scripting.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.composable.Deferred;
import reactor.core.composable.Promise;
import reactor.core.composable.spec.Promises;
import reactor.core.spec.Reactors;
import reactor.event.Event;
import reactor.js.core.JavaScriptObject;
import reactor.js.core.json.JsonFunctions;
import reactor.util.StringUtils;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

import static reactor.event.selector.Selectors.$;

/**
 * @author Jon Brisbin
 */
public class JavaScriptModuleLoader {

	private static final String PARENT_URI = "__parentUri";
	private static final String BASE_URI   = "__baseUri";

	private final Logger      log = LoggerFactory.getLogger(getClass());
	private final ClassLoader cl  = getClass().getClassLoader();

	private final Environment      env;
	private final List<String>     searchPaths;
	private final Reactor          loader;
	private final JavaScriptObject pkgDefCache;
	private final JavaScriptObject moduleCache;

	private volatile ScriptEngine engine;

	public JavaScriptModuleLoader(Environment env) {
		this(env, Arrays.asList(Paths.get(".").toAbsolutePath().toString(), "classpath://"));
	}

	public JavaScriptModuleLoader(Environment env, List<String> searchPaths) {
		this.env = env;
		this.searchPaths = searchPaths;

		this.loader = Reactors.reactor(env, Environment.THREAD_POOL);
		RequireFunction requireFn = new RequireFunction(searchPaths);
		this.loader.receive($("load"), (Event<String> ev) -> requireFn.apply(ev.getData()));

		this.pkgDefCache = new JavaScriptObject();
		this.moduleCache = new JavaScriptObject();
	}

	public JavaScriptModuleLoader setEngine(ScriptEngine engine) {
		this.engine = engine;
		return this;
	}

	public void remove(String id) {
		moduleCache.remove(id);
	}

	public Promise<JavaScriptModule> load(String id) {
		Deferred<Event<JavaScriptModule>, Promise<Event<JavaScriptModule>>> d = Promises.defer(env);
		JavaScriptModule mod;
		if (null == (mod = (JavaScriptModule) moduleCache.get(id))) {
			loader.sendAndReceive("load", Event.wrap(id), d);
		} else {
			d.accept(Event.wrap(mod));
		}
		return d.compose().map(Event::getData);
	}

	private JSObject loadPackageDefinition(String id) {
		return (JSObject) pkgDefCache.computeIfAbsent(id, s -> {
			JSObject pkgDef = null;
			for (String searchPath : searchPaths) {
				if (!searchPath.endsWith("/")) {
					searchPath += "/";
				}
				String baseUri;
				try {
					if (searchPath.startsWith("classpath:")) {
						baseUri = concatRelative(searchPath, id);
						if (baseUri.charAt(13) == '/') {
							baseUri = baseUri.substring(13);
						} else {
							baseUri = baseUri.substring(12);
						}
						// Read package.json to find what file to load
						String pkgJson = read(cl.getResourceAsStream(concatRelative(baseUri, "./package.json")));
						if (null == pkgJson) {
							continue;
						}
						pkgDef = JsonFunctions.parse(pkgJson);
					} else {
						baseUri = concatRelative(searchPath, id);
						// Read package.json to find what file to load
						pkgDef = readJSON(URI.create(concatRelative(baseUri, "./package.json")));
					}
					if (null != pkgDef) {
						pkgDef.setMember(PARENT_URI, searchPath);
						pkgDef.setMember(BASE_URI, searchPath + id);
						if (log.isDebugEnabled()) {
							log.debug("Read package.json: {}", pkgDef);
						}
						return pkgDef;
					}
				} catch (IOException e) {
					if (!e.getMessage().contains("No such file or directory")) {
						if (log.isDebugEnabled()) {
							log.debug(e.getMessage());
						}
					}
				}
			}

			return null;
		});
	}

	private JavaScriptModule loadModule(String id,
	                                    URI uri,
	                                    Function<String, JavaScriptModule> requireFn)
			throws ScriptException, IOException {
		return loadModule(id, uri, "load('" + uri + "');", requireFn);
	}

	private JavaScriptModule loadModule(String id,
	                                    URI uri,
	                                    String src,
	                                    Function<String, JavaScriptModule> requireFn)
			throws ScriptException, IOException {
		if ("classpath".equals(uri.getScheme())) {
			String realUri = uri.toString();
			int start = (realUri.charAt(12) == '/' ? 13 : 12);
			src = readJS(realUri.substring(start));
		}
		JavaScriptModule mod = new JavaScriptModule(id, uri);
		Bindings b = engine.createBindings();
		b.put("require", requireFn);
		JavaScriptObject exports = new JavaScriptObject();
		mod.put("exports", exports);
		b.put("exports", exports);
		b.put("module", mod);
		b.put("global", new HashMap<>()); // just gets thrown away since we don't support globals
		engine.eval(src, b);
		if (log.isDebugEnabled()) {
			log.debug("Loaded module: {}", mod);
		}
		return mod;
	}

	private String concatRelative(String... paths) {
		Queue<String> newParts = new ArrayDeque<>();
		for (String s : paths) {
			String cleaned = (s.endsWith("/") ? s.substring(0, s.length() - 1) : s);
			if (!s.startsWith(".")) {
				newParts.add(cleaned);
			} else if (cleaned.startsWith("./")) {
				newParts.add(s.substring(2));
			} else if (cleaned.startsWith("../")) {
				try {
					newParts.remove();
				} catch (NoSuchElementException ignored) {
				}
				newParts.add(cleaned.substring(3));
			} else {
				newParts.add(cleaned);
			}
		}
		return StringUtils.collectionToDelimitedString(newParts, "/");
	}

	private String read(InputStream in) throws IOException {
		if (null == in) {
			return null;
		}
		BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
		StringBuilder content = new StringBuilder();
		for (String line = rdr.readLine(); null != line; line = rdr.readLine()) {
			content.append(line).append("\n");
		}
		return content.toString();
	}

	private JSObject readJSON(URI src) throws IOException {
		return JsonFunctions.parse(read(src.toURL().openStream()));
	}

	private String readJS(String resource) throws IOException {
		return read(cl.getResourceAsStream(resource));
	}

	private class RequireFunction implements Function<String, JavaScriptModule> {
		private final List<String> searchPaths;

		private RequireFunction(List<String> searchPaths) {
			this.searchPaths = searchPaths;
		}

		@Override
		public JavaScriptModule apply(String id) {
			JavaScriptModule mod;
			JSObject pkgDef;
			if (null == (pkgDef = loadPackageDefinition(id))) {
				// Not packaged. It might be $id.js or $id/index.js (or it's not found)
				for (String searchPath : searchPaths) {
					String[] permutations = new String[]{
							concatRelative(searchPath, id),
							concatRelative(searchPath, id, "./index")
					};
					for (String s : permutations) {
						URI u = URI.create((!s.endsWith(".js") ? s + ".js" : s));
						try {
							// Set up new search paths starting with the current one
							List<String> newSearchPaths = new ArrayList<>();
							newSearchPaths.add(s.substring(0, s.lastIndexOf('/')));
							newSearchPaths.add(s);
							newSearchPaths.addAll(searchPaths);
							mod = loadModule(id, u, new RequireFunction(newSearchPaths));
							if (null != mod) {
								return mod;
							}
						} catch (Exception e) {
							if (log.isDebugEnabled()) {
								log.debug("Tried loading {} but got: {}", u, e.getMessage());
							}
						}
					}
				}
			} else {
				String parentUri = pkgDef.getMember(PARENT_URI).toString();
				if (!parentUri.endsWith("/")) {
					parentUri += "/";
				}
				String baseUri = pkgDef.getMember(BASE_URI).toString();
				if (!baseUri.endsWith("/")) {
					baseUri += "/";
				}

				List<String> newSearchPaths = new ArrayList<>();
				newSearchPaths.add(baseUri);
				newSearchPaths.add(parentUri);
				newSearchPaths.addAll(searchPaths);

				String mainJsUri;
				String mainJsProp = pkgDef.getMember("main").toString();
				if (mainJsProp.startsWith("../")) {
					mainJsUri = concatRelative(parentUri, mainJsProp);
				} else {
					mainJsUri = concatRelative(baseUri, mainJsProp);
				}
				URI u = URI.create((mainJsUri.endsWith(".js") ? mainJsUri : mainJsUri + ".js"));
				try {
					return loadModule(id, u, new RequireFunction(newSearchPaths));
				} catch (Exception e) {
					if (log.isDebugEnabled()) {
						log.debug("Tried loading {} but got: {}", u, e.getMessage());
					}
				}
			}
			return null;
		}
	}

}
