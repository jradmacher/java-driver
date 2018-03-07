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
package com.datastax.oss.driver.api.querybuilder.select;

import com.datastax.oss.driver.api.querybuilder.BindMarker;

/**
 * A SELECT query that accepts additional clauses: WHERE, GROUP BY, ORDER BY, LIMIT, PER PARTITION
 * LIMIT, ALLOW FILTERING.
 */
public interface CanAddClause {

  // Implementation note - this interface is separate from CanAddSelector to make the following a
  // compile-time error:
  // selectFrom("foo").allowFiltering().build()

  /**
   * Adds a LIMIT clause to this query with a literal value.
   *
   * <p>If this method or {@link #limit(BindMarker)} is called multiple times, the last value is
   * used.
   */
  Select limit(int limit);

  /**
   * Adds a LIMIT clause to this query with a bound value.
   *
   * <p>Use one of the static factory method in {@link BindMarker} to create the argument.
   *
   * <p>If this method or {@link #limit(int)} is called multiple times, the last value is used.
   */
  Select limit(BindMarker bindMarker);

  /**
   * Adds an ALLOW FILTERING clause to this query.
   *
   * <p>This method is idempotent, calling it multiple times will only add a single clause.
   */
  Select allowFiltering();
}
