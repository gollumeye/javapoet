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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;

import static com.squareup.javapoet.Util.characterLiteralWithoutSingleQuotes;
import static com.squareup.javapoet.Util.checkArgument;
import static com.squareup.javapoet.Util.checkNotNull;

/** A generated annotation on a declaration. */
public final class AnnotationSpec {
  public static final String VALUE = "value";
  private final AnnotationFormatter formatter;

  public final TypeName type;
  public final Map<String, List<CodeBlock>> members;

  private AnnotationSpec(Builder builder) {
    this.type = builder.type;
    this.members = Util.immutableMultimap(builder.members);
    this.formatter = builder.formatter != null
            ? builder.formatter
            : new DefaultAnnotationFormatter();
  }

  void emit(CodeWriter codeWriter, boolean inline) throws IOException {
    formatter.format(this, codeWriter, inline);
  }

  public static AnnotationSpec get(Annotation annotation) {
    return AnnotationSpecConversionUtils.fromAnnotation(annotation, false);
  }

  public static AnnotationSpec get(Annotation annotation, boolean includeDefaultValues) {
    return AnnotationSpecConversionUtils.fromAnnotation(annotation, includeDefaultValues);
  }

  public static AnnotationSpec get(AnnotationMirror annotation) {
    return AnnotationSpecConversionUtils.fromAnnotationMirror(annotation);
  }

  public static Builder builder(ClassName type) {
    checkNotNull(type, "type == null");
    return new Builder(type);
  }

  public static Builder builder(Class<?> type) {
    return builder(ClassName.get(type));
  }

  public Builder toBuilder() {
    Builder builder = new Builder(type);
    for (Map.Entry<String, List<CodeBlock>> entry : members.entrySet()) {
      builder.members.put(entry.getKey(), new ArrayList<>(entry.getValue()));
    }
    builder.formatter(formatter);
    return builder;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (getClass() != o.getClass()) return false;
    return toString().equals(o.toString());
  }

  @Override public int hashCode() {
    return toString().hashCode();
  }

  @Override public String toString() {
    StringBuilder out = new StringBuilder();
    try {
      CodeWriter codeWriter = new CodeWriter(out);
      codeWriter.emit("$L", this);
      return out.toString();
    } catch (IOException e) {
      throw new AssertionError();
    }
  }

  public static final class Builder {
    private final TypeName type;
    public final Map<String, List<CodeBlock>> members = new LinkedHashMap<>();
    private AnnotationFormatter formatter;

    private Builder(TypeName type) {
      this.type = type;
    }

    public Builder addMember(String name, String format, Object... args) {
      return addMember(name, CodeBlock.of(format, args));
    }

    public Builder addMember(String name, CodeBlock codeBlock) {
      List<CodeBlock> values = members.computeIfAbsent(name, k -> new ArrayList<>());
      values.add(codeBlock);
      return this;
    }

    public Builder formatter(AnnotationFormatter formatter) {
      this.formatter = formatter;
      return this;
    }

    /**
     * Delegates to {@link #addMember(String, String, Object...)}, with parameter {@code format}
     * depending on the given {@code value} object. Falls back to {@code "$L"} literal format if
     * the class of the given {@code value} object is not supported.
     */
    Builder addMemberForValue(String memberName, Object value) {
      checkNotNull(memberName, "memberName == null");
      checkNotNull(value, "value == null, constant non-null value expected for %s", memberName);
      checkArgument(SourceVersion.isName(memberName), "not a valid name: %s", memberName);
      if (value instanceof Class<?>) {
        return addMember(memberName, "$T.class", value);
      }
      if (value instanceof Enum) {
        return addMember(memberName, "$T.$L", value.getClass(), ((Enum<?>) value).name());
      }
      if (value instanceof String) {
        return addMember(memberName, "$S", value);
      }
      if (value instanceof Float) {
        return addMember(memberName, "$Lf", value);
      }
      if (value instanceof Long) {
        return addMember(memberName, "$LL", value);
      }
      if (value instanceof Character) {
        return addMember(memberName, "'$L'", characterLiteralWithoutSingleQuotes((char) value));
      }
      return addMember(memberName, "$L", value);
    }

    public AnnotationSpec build() {
      for (String name : members.keySet()) {
        checkNotNull(name, "name == null");
        checkArgument(SourceVersion.isName(name), "not a valid name: %s", name);
      }
      return new AnnotationSpec(this);
    }
  }
}
