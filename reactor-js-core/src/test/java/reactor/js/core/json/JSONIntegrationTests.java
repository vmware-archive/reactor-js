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

import jdk.nashorn.internal.parser.JSONParser;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.Source;
import org.junit.Test;
import reactor.js.core.AbstractJavaScriptTest;
import reactor.js.core.JavaScriptArray;

import javax.script.ScriptException;
import java.util.Collection;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Jon Brisbin
 */
public class JSONIntegrationTests extends AbstractJavaScriptTest {

	public final static String DOCUMENT =
			"{ \"store\": {\n" +
					"    \"book\": [ \n" +
					"      { \"category\": \"reference\",\n" +
					"        \"author\": \"Nigel Rees\",\n" +
					"        \"title\": \"Sayings of the Century\",\n" +
					"        \"price\": 8.95\n" +
					"      },\n" +
					"      { \"category\": \"fiction\",\n" +
					"        \"author\": \"Evelyn Waugh\",\n" +
					"        \"title\": \"Sword of Honour\",\n" +
					"        \"price\": 12.99\n" +
					"      },\n" +
					"      { \"category\": \"fiction\",\n" +
					"        \"author\": \"Herman Melville\",\n" +
					"        \"title\": \"Moby Dick\",\n" +
					"        \"isbn\": \"0-553-21311-3\",\n" +
					"        \"price\": 8.99\n" +
					"      },\n" +
					"      { \"category\": \"fiction\",\n" +
					"        \"author\": \"J. R. R. Tolkien\",\n" +
					"        \"title\": \"The Lord of the Rings\",\n" +
					"        \"isbn\": \"0-395-19395-8\",\n" +
					"        \"price\": 22.99\n" +
					"      }\n" +
					"    ],\n" +
					"    \"bicycle\": {\n" +
					"      \"color\": \"red\",\n" +
					"      \"price\": 19.95,\n" +
					"      \"foo:bar\": \"fooBar\",\n" +
					"      \"dot.notation\": \"new\"\n" +
					"    }\n" +
					"  }\n" +
					"}";

	@Test
	public void parsesJsonDocument() throws ScriptException {
		engine.eval("var JSON = Java.type(\"reactor.js.core.json.JsonFunctions\");", bindings);
		bindings.put("jsonStr", DOCUMENT);

		Object obj = engine.eval("var jsonObj = JSON.parse(jsonStr);jsonObj", bindings);
		assertNotNull("Object was parsed", obj);

		Double dbl = (Double) engine.eval("jsonObj.store.book[0].price;", bindings);
		assertThat("Value was extracted", dbl, is(8.95));
	}
	
	@Test
    public void parsesAndStringifysJsonDocument() throws ScriptException {
        engine.eval("var JSON = Java.type(\"reactor.js.core.json.JsonFunctions\");", bindings);
        bindings.put("jsonStr", DOCUMENT);

        Object obj = engine.eval("var jsonObj = JSON.parse(jsonStr);jsonObj", bindings);
        assertNotNull("Object was parsed", obj);
        
        obj = engine.eval("jsonStr = JSON.stringify(jsonObj);jsonStr", bindings);
        assertNotNull("Object was stringified", obj);
        
        obj = engine.eval("jsonObj = JSON.parse(jsonStr);jsonObj", bindings);
        assertNotNull("Object was re-parsed", obj);

        Double dbl = (Double) engine.eval("jsonObj.store.book[0].price;", bindings);
        assertThat("Value was extracted", dbl, is(8.95));
    }
	
	@Test
    public void parsesIntoNativeArray() throws ScriptException {
	    //engine.eval("var JSON = Java.type(\"reactor.js.core.json.JsonFunctions\");", bindings);
	    engine.eval("var JSON = Java.type(\"com.pivotal.cf.mobile.ats.json.JsonFunctions\");", bindings);
        bindings.put("jsonStr", DOCUMENT);

        Object obj = engine.eval("var jsonObj = JSON.parse(jsonStr);jsonObj", bindings);
        assertNotNull("Object was parsed", obj);

        Double dbl = (Double) engine.eval("jsonObj.store.book.splice(0,1)[0].price;", bindings);
        assertThat("Value was extracted", dbl, is(8.95));
    }

	@SuppressWarnings("unchecked")
	@Test
	public void performsJsonPathOnObjects() throws ScriptException {
		bindings.put("jsonStr", DOCUMENT);
		String js = "JSON.path(\"$.store.book[*]\").apply(JSON.parse(jsonStr));";

		engine.eval("var JSON = Java.type(\"reactor.js.core.json.JsonFunctions\");", bindings);
		JavaScriptArray ary = (JavaScriptArray) engine.eval(js, bindings);
		assertThat("Results were returned", ary.values(), not(empty()));
	}

	@Test
	public void jsonParseThroughput() throws Exception {
		final long timeout = 5000;
		bindings.put("jsonStr", DOCUMENT);

		//engine.eval("var JSON = Java.type(\"reactor.js.core.json.JsonFunctions\");", bindings);
		doThroughputTest("JSON.parse()", () -> engine.eval("JSON.parse(jsonStr);", bindings), timeout);

		final ErrorManager errorManager = new Context.ThrowErrorManager();
		doThroughputTest("new JSONParser().parse()",
		                 () -> new JSONParser(new Source("<json>", DOCUMENT), errorManager).parse(),
		                 timeout);

		doThroughputTest("JsonFunctions.parse()", () -> JsonFunctions.parse(DOCUMENT), timeout);
	}

}
