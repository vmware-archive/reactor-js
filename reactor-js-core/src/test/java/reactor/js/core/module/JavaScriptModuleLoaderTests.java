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

import org.junit.Before;
import org.junit.Test;
import reactor.core.Environment;
import reactor.js.core.AbstractJavaScriptTest;

import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;

/**
 * @author Jon Brisbin
 */
public class JavaScriptModuleLoaderTests extends AbstractJavaScriptTest {

	Environment            env;
	JavaScriptModuleLoader loader;

	@Before
	public void setup() {
		super.setup();
		env = new Environment();
		loader = new JavaScriptModuleLoader(env, Arrays.asList("classpath://",
		                                                       Paths.get("reactor-js-core/src/test/js")
		                                                            .toAbsolutePath()
		                                                            .toUri()
		                                                            .toString(),
		                                                       Paths.get("src/test/js")
		                                                            .toAbsolutePath()
		                                                            .toUri()
		                                                            .toString(),
		                                                       "https://raw.githubusercontent.com"
		));
		loader.setEngine(engine);
	}

	@Test(timeout = 5000)
	public void testModuleLoaderLoadsPackagedModules() throws InterruptedException {
		JavaScriptModule mod = loader.load("modulewithmain").await();

		assertNotNull("Module was found", mod);
	}

	@Test(timeout = 5000)
	public void testModuleLoaderLoadsModulesWithIndex() throws InterruptedException {
		JavaScriptModule mod = loader.load("modulewithindex").await();

		assertNotNull("Module was found", mod);
	}

	@Test(timeout = 5000)
	public void testModuleLoaderLoadsModulesInPath() throws InterruptedException {
		JavaScriptModule mod = loader.load("moduleinpath").await();

		assertNotNull("Module was found", mod);
	}

	@Test(timeout = 30000)
	public void testModuleLoaderLoadsPackagedModulesOverHttp() throws InterruptedException {
		JavaScriptModule mod = loader.load("cujojs/jiff/master").await();

		assertNotNull("Module was found", mod);
	}

}
