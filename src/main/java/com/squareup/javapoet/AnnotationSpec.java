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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;

import static com.squareup.javapoet.Util.checkArgument;
import static com.squareup.javapoet.Util.checkNotNull;

/** A generated annotation on a declaration. */
public final class AnnotationSpec {
  public static final String VALUE = "value";
  private final AnnotationFormatter formatter;

  public final TypeNameProvider type;
  public final Map<String, List<CodeBlock>> members;

  private AnnotationSpec(Builder builder) {
    this.type = builder.type;
    this.members = builder.members;
    this.formatter = builder.formatter != null
            ? builder.formatter
            : new DefaultAnnotationFormatter();
  }

  public void emit(CodeWriter codeWriter, boolean inline) throws IOException {
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
    builder.memberManager = new AnnotationMemberManager(new LinkedHashMap<>(members));
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
    private final TypeNameProvider type;
    public Map<String, List<CodeBlock>> members = new LinkedHashMap<>();
    private AnnotationFormatter formatter;
    private AnnotationMemberManager memberManager;

    private Builder(TypeNameProvider type) {
      this.type = type;
      this.memberManager = new AnnotationMemberManager(members);
    }

    public Builder addMember(String name, String format, Object... args) {
      memberManager.addMember(name, format, args);
      return this;
    }

    public Builder formatter(AnnotationFormatter formatter) {
      this.formatter = formatter;
      return this;
    }

    Builder addMemberForValue(String memberName, Object value) {
      memberManager.addMemberForValue(memberName, value);
      return this;
    }

    public AnnotationSpec build() {
      members = memberManager.getMembers();
      for (String name : members.keySet()) {
        checkNotNull(name, "name == null");
        checkArgument(SourceVersion.isName(name), "not a valid name: %s", name);
      }
      return new AnnotationSpec(this);
    }
  }
}
