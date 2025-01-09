/*
 * Copyright (C) 2015 Square, Inc.
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
package com.squareup.javapoet;

import com.squareup.javapoet.codewriter.CodeWriter;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import javax.lang.model.element.Modifier;
import java.util.Collections;

public class FieldSpecTest {
  @Test public void equalsAndHashCode() {
    FieldSpec a = FieldSpec.builder(int.class, "foo").build();
    FieldSpec b = FieldSpec.builder(int.class, "foo").build();
    assertThat(a.equals(b)).isTrue();
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
    assertThat(a.toString()).isEqualTo(b.toString());
    a = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
    b = FieldSpec.builder(int.class, "FOO", Modifier.PUBLIC, Modifier.STATIC).build();
    assertThat(a.equals(b)).isTrue();
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
    assertThat(a.toString()).isEqualTo(b.toString());
  }

  @Test public void nullAnnotationsAddition() {
    try {
      FieldSpec.builder(int.class, "foo").addAnnotations(null);
      fail();
    }
    catch (IllegalArgumentException expected) {
      assertThat(expected.getMessage())
          .isEqualTo("annotationSpecs == null");
    }
  }

  @Test public void modifyAnnotations() {
    FieldSpec.Builder builder = FieldSpec.builder(int.class, "foo")
          .addAnnotation(Override.class)
          .addAnnotation(SuppressWarnings.class);

    builder.annotations.remove(1);
    assertThat(builder.build().annotations).hasSize(1);
  }

  @Test public void modifyModifiers() {
    FieldSpec.Builder builder = FieldSpec.builder(int.class, "foo")
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

    builder.modifiers.remove(1);
    assertThat(builder.build().modifiers).containsExactly(Modifier.PUBLIC);
  }

  @Test
  public void initializerSetOnce() {
    FieldSpec.Builder builder = FieldSpec.builder(int.class, "foo")
            .initializer("$L", 42);
    try {
      builder.initializer("$L", 43);
      fail();
    } catch (IllegalStateException expected) {
      assertThat(expected.getMessage()).isEqualTo("initializer was already set");
    }
  }

  @Test
  public void testHasModifier() {
    FieldSpec field = FieldSpec.builder(int.class, "foo", Modifier.PUBLIC).build();
    assertThat(field.hasModifier(Modifier.PUBLIC)).isTrue();
    assertThat(field.hasModifier(Modifier.PRIVATE)).isFalse();
  }

  @Test
  public void toBuilderCopiesAll() {
    FieldSpec original = FieldSpec.builder(int.class, "foo", Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .addJavadoc("Test field")
            .initializer("$L", 123)
            .build();

    FieldSpec copy = original.toBuilder().build();
    assertThat(copy.toString()).isEqualTo(original.toString());
  }

  @Test
  public void emitFieldSpec() throws Exception {
    StringBuilder output = new StringBuilder();
    CodeWriter codeWriter = new CodeWriter(output);

    FieldSpec field = FieldSpec.builder(int.class, "foo", Modifier.PRIVATE)
            .initializer("$L", 42)
            .build();

    field.emit(codeWriter, Collections.emptySet());
    assertThat(output.toString()).isEqualTo("private int foo = 42;\n");
  }

  @Test
  public void testEmptyInitializer() {
    FieldSpec field = FieldSpec.builder(String.class, "foo").build();
    assertThat(field.initializer.isEmpty()).isTrue();
    assertThat(field.toString()).isEqualTo("java.lang.String foo;\n");
  }
}
