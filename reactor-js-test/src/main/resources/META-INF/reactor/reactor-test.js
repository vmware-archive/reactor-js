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

(function (global) {

  var LoggerFactory = Java.type("org.slf4j.LoggerFactory");
  var LOG = LoggerFactory.getLogger("reactor.js");

  global.console = {
    log: function () {
      LOG.info(arguments[0], arguments[1]);
    }
  };

  global.assertNotUndefined = function () {
    var msg = typeof arguments[0] === 'string' || '';
    assertTrue(msg, (arguments[1] ? true : false));
  }

})(this);