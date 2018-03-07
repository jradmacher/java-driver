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
package com.datastax.oss.driver.internal.querybuilder.select;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.BindMarker;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.select.SelectFrom;
import com.datastax.oss.driver.api.querybuilder.select.Selector;
import com.datastax.oss.driver.internal.core.metadata.schema.ScriptBuilder;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.function.Function;

public class DefaultSelect implements SelectFrom, Select {

  private static final ImmutableList<AliasedSelector> SELECT_ALL =
      ImmutableList.of(new AliasedSelector(ConstantSelector.ALL));

  private final CqlIdentifier keyspace;
  private final CqlIdentifier table;
  private final boolean isJson;
  private final boolean isDistinct;
  private final ImmutableList<AliasedSelector> selectors;
  private final Object limit;
  private final boolean allowFiltering;

  public DefaultSelect(CqlIdentifier keyspace, CqlIdentifier table) {
    this(keyspace, table, false, false, ImmutableList.of(), null, false);
  }

  private DefaultSelect(
      CqlIdentifier keyspace,
      CqlIdentifier table,
      boolean isJson,
      boolean isDistinct,
      ImmutableList<AliasedSelector> selectors,
      Object limit,
      boolean allowFiltering) {
    this.keyspace = keyspace;
    this.table = table;
    this.isJson = isJson;
    this.isDistinct = isDistinct;
    this.selectors = selectors;
    this.limit = limit;
    this.allowFiltering = allowFiltering;
  }

  @Override
  public SelectFrom json() {
    return new DefaultSelect(keyspace, table, true, isDistinct, selectors, limit, allowFiltering);
  }

  @Override
  public SelectFrom distinct() {
    return new DefaultSelect(keyspace, table, isJson, true, selectors, limit, allowFiltering);
  }

  public Select selector(Selector selector) {
    ImmutableList<AliasedSelector> newSelectors;
    if (selector == ConstantSelector.ALL) {
      // '*' cancels any previous one
      newSelectors = SELECT_ALL;
    } else if (SELECT_ALL.equals(selectors)) {
      // previous '*' gets cancelled
      newSelectors = ImmutableList.of(new AliasedSelector(selector));
    } else {
      newSelectors = append(selectors, new AliasedSelector(selector));
    }
    return withSelectors(newSelectors);
  }

  @Override
  public Select selectors(Iterable<Selector> additionalSelectors) {
    ImmutableList.Builder<AliasedSelector> newSelectors = ImmutableList.builder();
    if (!SELECT_ALL.equals(selectors)) { // previous '*' gets cancelled
      newSelectors.addAll(selectors);
    }
    for (Selector selector : additionalSelectors) {
      if (selector == ConstantSelector.ALL) {
        throw new IllegalArgumentException("Can't pass the * selector to selectors()");
      }
      newSelectors.add(new AliasedSelector(selector));
    }
    return withSelectors(newSelectors.build());
  }

  @Override
  public Select as(CqlIdentifier alias) {
    if (SELECT_ALL.equals(selectors)) {
      throw new IllegalStateException("Can't alias the * selector");
    } else if (selectors.isEmpty()) {
      throw new IllegalStateException("Can't alias, no selectors defined");
    }
    return withSelectors(modifyLast(selectors, last -> last.as(alias)));
  }

  private Select withSelectors(ImmutableList<AliasedSelector> newSelectors) {
    return new DefaultSelect(
        keyspace, table, isJson, isDistinct, newSelectors, limit, allowFiltering);
  }

  @Override
  public Select limit(int limit) {
    Preconditions.checkArgument(limit > 0, "Limit must be strictly positive");
    return new DefaultSelect(keyspace, table, isJson, isDistinct, selectors, limit, allowFiltering);
  }

  @Override
  public Select limit(BindMarker bindMarker) {
    Preconditions.checkNotNull(bindMarker);
    return new DefaultSelect(
        keyspace, table, isJson, isDistinct, selectors, bindMarker, allowFiltering);
  }

  @Override
  public Select allowFiltering() {
    return new DefaultSelect(keyspace, table, isJson, isDistinct, selectors, limit, true);
  }

  @Override
  public SimpleStatement build(boolean pretty) {
    // TODO move ScriptBuilder to a more generic package (util?), it's now used in multiple places
    ScriptBuilder builder = new ScriptBuilder(pretty);

    builder.append("SELECT");
    if (isJson) {
      builder.append(" JSON");
    }
    if (isDistinct) {
      builder.append(" DISTINCT");
    }

    builder.increaseIndent();
    for (int i = 0; i < selectors.size(); i++) {
      if (i > 0) {
        builder.append(",");
      }
      builder.newLine().append(selectors.get(i).asCql(pretty));
    }
    builder.decreaseIndent();

    builder.newLine().append("FROM ");
    if (keyspace != null) {
      builder.append(keyspace).append(".");
    }
    builder.append(table);

    if (limit != null) {
      builder
          .newLine()
          .append("LIMIT ")
          .append(
              (limit instanceof BindMarker)
                  ? ((BindMarker) limit).asCql(pretty)
                  : limit.toString());
    }
    if (allowFiltering) {
      builder.newLine().append("ALLOW FILTERING");
    }

    return SimpleStatement.newInstance(builder.build());
  }

  // TODO will likely be used by other queries, move elsewhere
  private static <T> ImmutableList<T> append(ImmutableList<T> list, T newElement) {
    return ImmutableList.<T>builder().addAll(list).add(newElement).build();
  }

  private static <T> ImmutableList<T> modifyLast(ImmutableList<T> list, Function<T, T> change) {
    ImmutableList.Builder<T> builder = ImmutableList.builder();
    int size = list.size();
    for (int i = 0; i < size - 1; i++) {
      builder.add(list.get(i));
    }
    builder.add(change.apply(list.get(size - 1)));
    return builder.build();
  }
}
