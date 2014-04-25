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
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornException;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.internal.runtime.Undefined;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jon Brisbin
 */
public class NashornJUnitTestRunner extends BlockJUnit4ClassRunner {

	private static final Logger LOG = LoggerFactory.getLogger(NashornJUnitTestRunner.class);

	private final NashornScriptEngine engine;
	private final Bindings            bindings;
	private final CompiledScript      script;

	public NashornJUnitTestRunner(Class<?> testType) throws InitializationError, ScriptException {
		super(testType);

		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("nashorn");
		if (null == engine) {
			throw new IllegalStateException("Nashorn JavaScript engine not available.");
		}
		this.engine = (NashornScriptEngine) engine;
		this.bindings = engine.createBindings();

		StringBuilder loadJs = new StringBuilder();
		loadJs.append("load(\"classpath:META-INF/reactor/reactor-test.js\");\n");

		JavaScriptTests testsAnno = testType.getDeclaredAnnotation(JavaScriptTests.class);
		if (testsAnno.paths().length > 0) {
			loadJs.append("require.paths=[\".\"");
			for (String loadPath : testsAnno.paths()) {
				loadJs.append(",\"").append(loadPath).append("\"");
			}
			loadJs.append("];\n");
		}
		for (String script : testsAnno.value()) {
			loadJs.append("require(\"").append(script).append("\");\n");
		}
		if (!"".equals(testsAnno.module())) {
			String moduleName = testsAnno.module();
			loadJs.append("require(\"" + moduleName + "\");\n");
		}

		addStaticMethods(Assert.class, bindings);
		addStaticMethods(CoreMatchers.class, bindings);

		bindings.put("require", new RequireFunction(engine, bindings));

		this.script = this.engine.compile(loadJs.toString());
	}

	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
		super.runChild(method, notifier);
	}

	@Override
	protected Statement methodInvoker(FrameworkMethod method, Object test) {
		return new ECMAScriptStatement(method, test);
	}

	private static void addStaticMethods(Class<?> type, Bindings bindings) {
		for (Method method : type.getDeclaredMethods()) {
			if (Modifier.isStatic(method.getModifiers())) {
				final Method methodToInvoke = method;
				method.setAccessible(true);
				bindings.put(method.getName(),
				             new AbstractJSObject() {
					             @Override
					             public Object call(Object thiz, Object... args) {
						             try {
							             return methodToInvoke.invoke(null, args);
						             } catch (Exception e) {
							             return new NashornException(e.getMessage(), e) {
							             };
						             }
					             }

					             @Override
					             public boolean isFunction() {
						             return true;
					             }

					             @Override
					             public boolean isStrictFunction() {
						             return true;
					             }
				             }
				);
			}
		}
	}

	private class ECMAScriptStatement extends Statement {
		private final FrameworkMethod method;
		private final Object          test;

		private Field testResultField;
		private List<Field> instanceFields = new ArrayList<>();

		private ECMAScriptStatement(FrameworkMethod method, Object test) {
			this.method = method;
			this.test = test;

			for (Field field : test.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				TestResult testResult;
				if (null != (testResult = field.getDeclaredAnnotation(TestResult.class))) {
					testResultField = field;
				} else {
					instanceFields.add(field);
				}
			}
		}

		@Override
		public void evaluate() throws Throwable {
			// set instance fields for this invocation
			for (Field field : instanceFields) {
				bindings.put(field.getName(), field.get(test));
			}

			ScriptContext ctx = new SimpleScriptContext();
			ctx.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
			script.eval(ctx);

			// get JavaScript test method
			JSObject jsTestMethod = (JSObject) bindings.get(method.getName());
			Object result = jsTestMethod.call(bindings);

			// transfer the result to the JUnit test instance field
			if (null != testResultField && !(result instanceof Undefined)) {
				testResultField.set(test, result);
			}

			// run the Java test
			method.invokeExplosively(test);

			// clean up instance fields
			for (Field field : instanceFields) {
				bindings.remove(field.getName());
			}
		}
	}

}
