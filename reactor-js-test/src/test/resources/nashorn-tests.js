require.paths = ["reactor-js-test/src/test/resources", "src/test/resources"];

var ext = require("helper");
assertNotUndefined("helper extension was loaded", ext.helloWorld);

(function (global) {

  global.firstTest = function () {
    assertThat("greeting came test class", global.greeting, is("Hello World!"));

    var hw = ext.helloWorld();
    assertThat("extension says hello", hw, is("Hello World!"));

    return new ext.Helper(hw);
  }

})(this);


