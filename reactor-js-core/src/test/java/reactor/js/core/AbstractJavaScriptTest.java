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

package reactor.js.core;

import org.junit.After;
import org.junit.Before;
import reactor.util.Assert;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.concurrent.Callable;

/**
 * @author Jon Brisbin
 */
public abstract class AbstractJavaScriptTest {

	protected ScriptEngine engine;
	protected Bindings     bindings;

	@Before
	public void setup() {
		ScriptEngineManager mgr = new ScriptEngineManager();
		engine = mgr.getEngineByName("nashorn");
		Assert.notNull(engine, "Nashorn JavaScript engine not available");
		bindings = engine.createBindings();
	}

	@After
	public void cleanup() {
	}

	protected void doThroughputTest(String name, Callable test, long timeout) throws Exception {
		long count = 0;
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < timeout) {
			assert null != test.call();
			count++;
		}
		long end = System.currentTimeMillis();
		double elapsed = end - start;
		long throughput = (long) (count / (elapsed / 1000));

		System.out.println(name + " throughput: " + throughput + "/s");
	}

}
