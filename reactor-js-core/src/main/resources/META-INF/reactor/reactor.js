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

(function () {

  var LoggerFactory = Java.type("org.slf4j.LoggerFactory");
  var UUIDUtils = Java.type("reactor.util.UUIDUtils");
  var Consumer = Java.type("reactor.function.Consumer");
  var Environment = Java.type("reactor.core.Environment");
  var Reactors = Java.type("reactor.core.spec.Reactors");
  var JavaScriptReactor = Java.type("reactor.js.core.JavaScriptReactor");

  var LOG = LoggerFactory.getLogger("reactor.js");
  var ENV = new Environment();

  var activeReactors = {};

  var createReactor = function (args) {
    var opts = args || {};
    var name = opts.name || UUIDUtils.random().toString();
    var dispatcher = opts.dispatcher || 'ringBuffer';

    var r = Reactors.reactor(ENV, dispatcher);
    LOG.debug("Registering Reactor {}...", name);
    activeReactors[name] = r;

    return new JavaScriptReactor(r);
  }

  return {
    create: createReactor
  };
})();