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
import com.datastax.oss.driver.api.querybuilder.select.SelectFrom;
import com.datastax.oss.driver.internal.querybuilder.select.DefaultSelect;

/** A Domain-Specific Language to build CQL queries using Java code. */
public interface QueryBuilderDsl {

  /** Starts a SELECT query for a qualified table. */
  static SelectFrom selectFrom(CqlIdentifier keyspace, CqlIdentifier table) {
    return new DefaultSelect(keyspace, table);
  }

  /**
   * Shortcut for {@link #selectFrom(CqlIdentifier, CqlIdentifier)
   * selectFrom(CqlIdentifier.fromCql(keyspace), CqlIdentifier.fromCql(table))}
   */
  static SelectFrom selectFrom(String keyspace, String table) {
    return selectFrom(CqlIdentifier.fromCql(keyspace), CqlIdentifier.fromCql(table));
  }

  /** Starts a SELECT query for an unqualified table. */
  static SelectFrom selectFrom(CqlIdentifier table) {
    return selectFrom(null, table);
  }

  /** Shortcut for {@link #selectFrom(CqlIdentifier) selectFrom(CqlIdentifier.fromCql(table))} */
  static SelectFrom selectFrom(String table) {
    return selectFrom(CqlIdentifier.fromCql(table));
  }
}
