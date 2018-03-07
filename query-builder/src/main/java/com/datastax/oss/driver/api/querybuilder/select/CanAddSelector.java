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

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.internal.querybuilder.select.ColumnSelector;
import com.datastax.oss.driver.internal.querybuilder.select.ConstantSelector;
import com.datastax.oss.driver.internal.querybuilder.select.RawSelector;
import java.util.Arrays;

/**
 * A SELECT query that accepts additional selectors (that is, elements in the SELECT clause to
 * return as columns in the result set, as in: {@code SELECT count(*), sku, price...}).
 */
public interface CanAddSelector {

  /**
   * Adds a selector. You probably want to use one of the convenience shortcuts instead: {@link
   * #all()}, {@link #countAll()}, {@link #column(CqlIdentifier)} etc.
   */
  Select selector(Selector selector);

  /**
   * Adds multiple selectors at once.
   *
   * <p>This is slightly more efficient than adding the selectors one by one (since the underlying
   * implementation of this object is immutable).
   *
   * <p>Use the static factory methods in {@link Selector} to create the arguments.
   *
   * @throws IllegalArgumentException if one of the selectors is {@link Selector#all()} ({@code *}
   *     can only be used on its own).
   */
  Select selectors(Iterable<Selector> selectors);

  /** Var-arg equivalent of {@link #selectors(Iterable)}. */
  default Select selectors(Selector... selectors) {
    return selectors(Arrays.asList(selectors));
  }

  /**
   * Selects all columns, as in {@code SELECT *}.
   *
   * <p>This will clear any previously configured selector. Similarly, if any other selector is
   * added later, it will cancel this one.
   */
  default Select all() {
    return selector(ConstantSelector.ALL);
  }

  /** Selects the count of all returned rows, as in {@code SELECT count(*)}. */
  default Select countAll() {
    return selector(ConstantSelector.COUNT_ALL);
  }

  /** Selects a particular column by its CQL identifier. */
  default Select column(CqlIdentifier columnId) {
    return selector(new ColumnSelector(columnId));
  }

  /** Shortcut for {@link #column(CqlIdentifier) column(CqlIdentifier.fromCql(columnName))} */
  default Select column(String columnName) {
    return column(CqlIdentifier.fromCql(columnName));
  }

  /**
   * Selects an arbitrary expression expressed as a raw string.
   *
   * <p>The contents be appended to the query as-is, without any syntax checking or escaping. This
   * method should be used with caution, as it's possible to generate invalid CQL that will fail at
   * execution time; on the other hand, it can be used as an "escape hatch" to handle edge cases
   * that are not covered by the query builder.
   */
  default Select raw(String rawExpression) {
    return selector(new RawSelector(rawExpression));
  }

  /**
   * Aliases the last added selector, as in {@code SELECT count(*) AS total}.
   *
   * <p>It is the caller's responsibility to ensure that this method is called at most once after
   * each selector, and that this selector can legally be aliased:
   *
   * <ul>
   *   <li>if it is called multiple times ({@code countAll().as("total1").as("total2")}), the last
   *       alias will override the previous ones.
   *   <li>if it is called before any selector was set, or after {@link #all()}, an {@link
   *       IllegalStateException} is thrown.
   *   <li>if it is called after a {@link #raw(String)} selector that already defines an alias, the
   *       query will fail at runtime.
   * </ul>
   */
  Select as(CqlIdentifier alias);

  /** Shortcut for {@link #as(CqlIdentifier) as(CqlIdentifier.fromCql(alias))} */
  default Select as(String alias) {
    return as(CqlIdentifier.fromCql(alias));
  }
}
