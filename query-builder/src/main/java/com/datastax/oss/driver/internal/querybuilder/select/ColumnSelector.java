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

public class ColumnSelector implements Selector {

  private final CqlIdentifier columnId;

  public ColumnSelector(CqlIdentifier columnId) {
    Preconditions.checkNotNull(columnId);
    this.columnId = columnId;
  }

  @Override
  public String asCql(boolean pretty) {
    return columnId.asCql(pretty);
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof ColumnSelector) {
      ColumnSelector that = (ColumnSelector) other;
      return this.columnId.equals(that.columnId);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return columnId.hashCode();
  }
}
