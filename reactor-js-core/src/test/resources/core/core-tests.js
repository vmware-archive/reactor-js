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
var reactor = load("classpath:META-INF/reactor/reactor.js");

canCreateReactor = function () {
  var r = reactor.create({ name: "reactor", dispatcher: "sync" });
  console.log("reactor: {}", r);

  r.on("test", function(ev) {
    console.log("ev: {}", ev);
  });

  r.notify("test", { data: "Hello World!" });
}