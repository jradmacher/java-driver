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
import com.datastax.oss.driver.api.querybuilder.select.Selector;
import com.google.common.base.Preconditions;
import java.util.Objects;

public class AliasedSelector {

  private final Selector selector;
  private final CqlIdentifier alias;

  public AliasedSelector(Selector selector) {
    this(selector, null);
  }

  private AliasedSelector(Selector selector, CqlIdentifier alias) {
    Preconditions.checkNotNull(selector);
    this.selector = selector;
    this.alias = alias;
  }

  public Selector getSelector() {
    return selector;
  }

  public CqlIdentifier getAlias() {
    return alias;
  }

  public AliasedSelector as(CqlIdentifier alias) {
    assert selector != ConstantSelector.ALL;
    return new AliasedSelector(selector, alias);
  }

  public String asCql(boolean pretty) {
    return alias == null
        ? selector.asCql(pretty)
        : selector.asCql(pretty) + " AS " + alias.asCql(pretty);
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof AliasedSelector) {
      AliasedSelector that = (AliasedSelector) other;
      return this.selector.equals(that.selector) && Objects.equals(this.alias, that.alias);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(selector, alias);
  }
}
