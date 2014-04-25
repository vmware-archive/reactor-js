# Reactor JavaScript (Nashorn) Support

This is the Nashorn-based JavaScript support for the [Reactor JVM framework](https://github.com/reactor/reactor).

### Core

TODO : Some of the testing components will be moved over to core at some point.

### Testing

These components provide helpers for running unit and integration tests in JUnit and providing bi-directional communication between Java and JavaScript.

To write tests in JavaScript and run them from JUnit, create a JavaScript file that exports a module for use by the test Runner (we'll call this `my-tests.js` and put it in the `src/test/resources` directory):

```javascript
var component = require("component");

(function (global) {

  global.jsIntegrationTest = function () {
    // do some testing
    var real = component.testMethod();

    assertThat("Result is as expected", real, is("expected"));
  }

})(this);
```

_Note:_ All static methods from `org.junit.Assert` and `org.hamcrest.CoreMatchers` are imported into the global namespace to make it easier to write assertions and matchers.

To access and interact with this test from JUnit, just create a normal JUnit test class but annotate it with a `@RunWith` annotation:

```java
import reactor.js.test.JavaScriptTests;
import reactor.js.test.NashornJUnitTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(NashornJUnitTestRunner.class)
@JavaScriptTests(module = "my-tests", paths = {"src/test/resources"})
public class MyIntegrationTests {

    @Test
    public void jsIntegrationTest() {}

}
```

When you run this JUnit test, it will bootstrap a Nashorn environment and provide all the necessary helpers to invoke your JavaScript. Your module is expected to export a function that matches the name of a method in the JUnit test class that is annotated with `@Test`.

Some things to note in this quick example:

* The `NashornJUnitTestRunner` will run "normal" JUnit tests if there is no matching JavaScript function.
* The test runner will run your test method AS WELL AS your JavaScript test function. If an error is thrown from either one, the test will fail.
* Bi-directional communication is possible between JUnit and JavaScript

#### Talking to JavaScript (and visa versa)

Bi-directional communication is possible with the `NashornJUnitTestRunner`. It will transfer your test class' properties into the JavaScript environment and the return value from your test function will be set into a field on your test class annotation with `@TestResult`.

Here is an example of extending the above test to talk to JavaScript:

```javascript
var component = require("component");

(function (global) {

  global.jsIntegrationTest = function () {
    // comes from MyIntegrationTests.greeting
    var greeting = global.greeting;

    // do some testing, passing the value from Java
    var real = component.testMethod(greeting);

    assertThat("Result is as expected", real, is("Hello World!"));

    return real;
  }

})(this);
```

```java
import reactor.js.test.JavaScriptTests;
import reactor.js.test.NashornJUnitTestRunner;
import reactor.js.test.TestResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(NashornJUnitTestRunner.class)
@JavaScriptTests(module = "my-tests", paths = {"src/test/resources"})
public class MyIntegrationTests {

    @TestResult
    String realResult;
    String greeting;

    @Before
    public void setup() {
        this.realResult = "Hello World!";
    }

    @Test
    public void jsIntegrationTest() {
        assertThat("JavaScript value was returned", realResult, is("Hello World!"));
    }

}
```

----
reactor-js is Apache 2.0 licensed.
