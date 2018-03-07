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

import static com.datastax.oss.driver.api.querybuilder.Assertions.assertThat;
import static com.datastax.oss.driver.api.querybuilder.BindMarker.bindMarker;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.selectFrom;
import static com.datastax.oss.driver.api.querybuilder.select.Selector.column;

import org.junit.Test;

public class SelectTest {

  @Test
  public void should_generate_queries() {
    SelectFrom selectFromFoo = selectFrom("foo");
    assertThat(selectFromFoo.all()).hasUglyCql("SELECT * FROM \"foo\"");
    assertThat(selectFromFoo.countAll()).hasUglyCql("SELECT count(*) FROM \"foo\"");
    assertThat(selectFromFoo.column("bar")).hasUglyCql("SELECT \"bar\" FROM \"foo\"");
    assertThat(selectFromFoo.column("bar").as("baz"))
        .hasUglyCql("SELECT \"bar\" AS \"baz\" FROM \"foo\"");
    assertThat(selectFromFoo.raw("a,b,c")).hasUglyCql("SELECT a,b,c FROM \"foo\"");
    assertThat(selectFromFoo.selectors(column("bar"), column("baz")))
        .hasUglyCql("SELECT \"bar\", \"baz\" FROM \"foo\"");
    assertThat(selectFromFoo.all().limit(1)).hasUglyCql("SELECT * FROM \"foo\" LIMIT 1");
    assertThat(selectFromFoo.all().limit(bindMarker("l")))
        .hasUglyCql("SELECT * FROM \"foo\" LIMIT :\"l\"");
  }

  @Test(expected = IllegalStateException.class)
  public void should_fail_to_alias_star_selector() {
    selectFrom("foo").all().as("allthethings");
  }

  @Test(expected = IllegalStateException.class)
  public void should_fail_to_alias_if_no_selector_yet() {
    selectFrom("foo").as("bar");
  }

  @Test
  public void should_keep_last_alias_if_aliased_twice() {
    assertThat(selectFrom("foo").countAll().as("allthethings").as("total"))
        .hasUglyCql("SELECT count(*) AS \"total\" FROM \"foo\"");
  }

  @Test
  public void should_remove_star_selector_if_other_selector_added() {
    assertThat(selectFrom("foo").all().column("bar")).hasUglyCql("SELECT \"bar\" FROM \"foo\"");
  }

  @Test
  public void should_remove_other_selectors_if_star_selector_added() {
    assertThat(selectFrom("foo").column("bar").column("baz").all())
        .hasUglyCql("SELECT * FROM \"foo\"");
  }

  @Test
  public void should_use_last_limit_if_called_multiple_times() {
    assertThat(selectFrom("foo").all().limit(1).limit(2))
        .hasUglyCql("SELECT * FROM \"foo\" LIMIT 2");
  }

  @Test
  public void should_use_single_allow_filtering_if_called_multiple_times() {
    assertThat(selectFrom("foo").all().allowFiltering().allowFiltering())
        .hasUglyCql("SELECT * FROM \"foo\" ALLOW FILTERING");
  }
}
