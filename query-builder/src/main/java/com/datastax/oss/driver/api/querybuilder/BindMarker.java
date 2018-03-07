/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.api.querybuilder;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.internal.querybuilder.select.DefaultBindMarker;

public interface BindMarker {

  /** Creates an anonymous bind marker, which appears as {@code ?} in the generated CQL. */
  static BindMarker bindMarker() {
    return bindMarker((CqlIdentifier) null);
  }

  /** Creates a named bind marker, which appears as {@code :id} in the generated CQL. */
  static BindMarker bindMarker(CqlIdentifier id) {
    return new DefaultBindMarker(id);
  }

  /** Shortcut for {@link #bindMarker(CqlIdentifier) bindMarker(CqlIdentifier.fromCql(name))} */
  static BindMarker bindMarker(String name) {
    return bindMarker(CqlIdentifier.fromCql(name));
  }

  String asCql(boolean pretty);
}
