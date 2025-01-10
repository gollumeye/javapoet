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
package com.squareup.javapoet.codewriter;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.LineWrapper;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.Multiset;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.ListIterator;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.lang.model.element.Modifier;

import static com.squareup.javapoet.Util.checkNotNull;
import static com.squareup.javapoet.Util.checkState;
import static com.squareup.javapoet.Util.stringLiteralWithDoubleQuotes;
import static java.lang.String.join;


public final class CodeWriter {
  private static final String NO_PACKAGE = new String();
  private static final Pattern LINE_BREAKING_PATTERN = Pattern.compile("\\R");

  private final IndentationManager indentationManager;
  private final LineWrapper out;
  private final StaticImportManager staticImportManager;
  private boolean javadoc = false;
  private boolean comment = false;
  private String packageName = NO_PACKAGE;
  private final List<TypeSpec> typeSpecStack = new ArrayList<>();
  private final Set<String> alwaysQualify;
  private final Map<String, ClassName> importedTypes;
  private final Map<String, ClassName> importableTypes = new LinkedHashMap<>();
  private final Set<String> referencedNames = new LinkedHashSet<>();
  private final Multiset<String> currentTypeVariables = new Multiset<>();
  private boolean trailingNewline;
  public int statementLine = -1;

  public CodeWriter(Appendable out) {
    this(out, "  ", Collections.emptySet(), Collections.emptySet());
  }

  public CodeWriter(Appendable out, String indent, Set<String> staticImports, Set<String> alwaysQualify) {
    this(out, indent, Collections.emptyMap(), staticImports, alwaysQualify);
  }

  public CodeWriter(Appendable out, String indent, Map<String, ClassName> importedTypes,
                    Set<String> staticImports, Set<String> alwaysQualify) {
    this.out = new LineWrapper(out, indent, 100);
    this.indentationManager = new IndentationManager(indent);
    this.staticImportManager = new StaticImportManager(staticImports);
    this.importedTypes = checkNotNull(importedTypes, "importedTypes == null");
    this.alwaysQualify = checkNotNull(alwaysQualify, "alwaysQualify == null");
  }

  public Map<String, ClassName> importedTypes() {
    return importedTypes;
  }

  public CodeWriter indent() {
    indentationManager.indent();
    return this;
  }

  public CodeWriter indent(int levels) {
    indentationManager.indent(levels);
    return this;
  }

  public CodeWriter unindent() {
    indentationManager.unindent();
    return this;
  }

  public CodeWriter unindent(int levels) {
    indentationManager.unindent(levels);
    return this;
  }

  public CodeWriter pushPackage(String packageName) {
    checkState(this.packageName.equals(NO_PACKAGE), "package already set: %s", this.packageName);
    this.packageName = checkNotNull(packageName, "packageName == null");
    return this;
  }

  public CodeWriter popPackage() {
    checkState(this.packageName != NO_PACKAGE, "package not set");
    this.packageName = NO_PACKAGE;
    return this;
  }

  public CodeWriter pushType(TypeSpec type) {
    this.typeSpecStack.add(type);
    return this;
  }

  public CodeWriter popType() {
    this.typeSpecStack.remove(typeSpecStack.size() - 1);
    return this;
  }

  public void emitComment(CodeBlock codeBlock) throws IOException {
    trailingNewline = true;
    comment = true;
    try {
      emit(codeBlock);
      emit("\n");
    } finally {
      comment = false;
    }
  }

  public void emitJavadoc(CodeBlock javadocCodeBlock) throws IOException {
    if (javadocCodeBlock.isEmpty()) return;

    emit("/**\n");
    javadoc = true;
    try {
      emit(javadocCodeBlock, true);
    } finally {
      javadoc = false;
    }
    emit(" */\n");
  }

  public void emitAnnotations(List<AnnotationSpec> annotations, boolean inline) throws IOException {
    for (AnnotationSpec annotationSpec : annotations) {
      annotationSpec.emit(this, inline);
      emit(inline ? " " : "\n");
    }
  }

  public void emitModifiers(Set<Modifier> modifiers, Set<Modifier> implicitModifiers) throws IOException {
    if (modifiers.isEmpty()) return;
    for (Modifier modifier : EnumSet.copyOf(modifiers)) {
      if (implicitModifiers.contains(modifier)) continue;
      emitAndIndent(modifier.name().toLowerCase(Locale.US));
      emitAndIndent(" ");
    }
  }

  public void emitModifiers(Set<Modifier> modifiers) throws IOException {
    emitModifiers(modifiers, Collections.emptySet());
  }

  public void emitTypeVariables(List<TypeVariableName> typeVariables) throws IOException {
    if (typeVariables.isEmpty()) return;

    typeVariables.forEach(typeVariable -> currentTypeVariables.add(typeVariable.name));

    emit("<");
    boolean firstTypeVariable = true;
    for (TypeVariableName typeVariable : typeVariables) {
      if (!firstTypeVariable) emit(", ");
      emitAnnotations(typeVariable.annotations, true);
      emit("$L", typeVariable.name);
      boolean firstBound = true;
      for (TypeName bound : typeVariable.bounds) {
        emit(firstBound ? " extends $T" : " & $T", bound);
        firstBound = false;
      }
      firstTypeVariable = false;
    }
    emit(">");
  }

  public void popTypeVariables(List<TypeVariableName> typeVariables) {
    typeVariables.forEach(typeVariable -> currentTypeVariables.remove(typeVariable.name));
  }

  public CodeWriter emit(String s) throws IOException {
    return emitAndIndent(s);
  }

  public CodeWriter emit(String format, Object... args) throws IOException {
    return emit(CodeBlock.of(format, args));
  }

  public CodeWriter emit(CodeBlock codeBlock) throws IOException {
    return emit(codeBlock, false);
  }

  public CodeWriter emit(CodeBlock codeBlock, boolean ensureTrailingNewline) throws IOException {
    int a = 0;
    ClassName deferredTypeName = null;
    ListIterator<String> partIterator = codeBlock.formatParts.listIterator();
    while (partIterator.hasNext()) {
      String part = partIterator.next();
      switch (part) {
        case "$L":
          emitLiteral(codeBlock.args.get(a++));
          break;
        case "$N":
          emitAndIndent((String) codeBlock.args.get(a++));
          break;
        case "$S":
          emitStringLiteral((String) codeBlock.args.get(a++));
          break;
        case "$T":
          deferredTypeName = emitTypeName(codeBlock, a++, partIterator, deferredTypeName);
          break;
        case "$$":
          emitAndIndent("$");
          break;
        case "$>":
          indent();
          break;
        case "$<":
          unindent();
          break;
        case "$[":
          startStatement();
          break;
        case "$]":
          endStatement();
          break;
        case "$W":
          out.wrappingSpace(indentationManager.getIndentLevel() + 2);
          break;
        case "$Z":
          out.zeroWidthSpace(indentationManager.getIndentLevel() + 2);
          break;
        default:
          boolean handled = handleDeferredTypeName(deferredTypeName, part);
          deferredTypeName = null;
          if (handled) {
            break;
          }
          emitAndIndent(part);
          break;
      }
    }
    if (ensureTrailingNewline && out.lastChar() != '\n') {
      emit("\n");
    }
    return this;
  }

  private void emitStringLiteral(String string) throws IOException {
    emitAndIndent(string != null ? stringLiteralWithDoubleQuotes(string, indentationManager.getIndent()) : "null");
  }

  private ClassName emitTypeName(CodeBlock codeBlock, int a, ListIterator<String> partIterator,
                                 ClassName deferredTypeName) throws IOException {
    TypeName typeName = (TypeName) codeBlock.args.get(a);
  if (typeName instanceof ClassName && partIterator.hasNext()
          && !codeBlock.formatParts.get(partIterator.nextIndex()).startsWith("$")) {
        ClassName candidate = (ClassName) typeName;
        if (staticImportManager.getStaticImportClassNames().contains(candidate.canonicalName)) {
          checkState(deferredTypeName == null, "pending type for static import?!");
          return candidate;
        }
    }
    typeName.emit(this);
    return null;
  }

  private void startStatement() {
    checkState(statementLine == -1, "statement enter $[ followed by statement enter $[");
    statementLine = 0;
  }

  private void endStatement() {
    checkState(statementLine != -1, "statement exit $] has no matching statement enter $[");
    if (statementLine > 0) {
      unindent(2);
    }
    statementLine = -1;
  }

  private boolean handleDeferredTypeName(ClassName deferredTypeName, String part) throws IOException {
    if (deferredTypeName != null) {
    if (part.startsWith(".") && staticImportManager.contains(deferredTypeName.canonicalName, part)) {
          emitAndIndent(part.substring(1));
          return true;
      }
      deferredTypeName.emit(this);
    }
    return false;
  }

  public CodeWriter emitWrappingSpace() throws IOException {
    out.wrappingSpace(indentationManager.getIndentLevel() + 2);
    return this;
  }

  public void emitLiteral(Object o) throws IOException {
    if (o instanceof TypeSpec) {
      ((TypeSpec) o).emit(this, null, Collections.emptySet());
    } else if (o instanceof AnnotationSpec) {
      ((AnnotationSpec) o).emit(this, true);
    } else if (o instanceof CodeBlock) {
      emit((CodeBlock) o);
    } else {
      emitAndIndent(String.valueOf(o));
    }
  }

  public String lookupName(ClassName className) {
    String topLevelSimpleName = className.topLevelClassName().simpleName();
    if (currentTypeVariables.contains(topLevelSimpleName)) {
      return className.canonicalName;
    }

    boolean nameResolved = false;
    for (ClassName c = className; c != null; c = c.enclosingClassName()) {
      ClassName resolved = resolve(c.simpleName());
      nameResolved = resolved != null;

      if (resolved != null && Objects.equals(resolved.canonicalName, c.canonicalName)) {
        int suffixOffset = c.simpleNames().size() - 1;
        return join(".", className.simpleNames().subList(suffixOffset, className.simpleNames().size()));
      }
    }

    if (nameResolved) {
      return className.canonicalName;
    }

    if (Objects.equals(packageName, className.packageName())) {
      referencedNames.add(topLevelSimpleName);
      return join(".", className.simpleNames());
    }

    if (!javadoc) {
      importableType(className);
    }

    return className.canonicalName;
  }

  private void importableType(ClassName className) {
    if (className.packageName().isEmpty() || alwaysQualify.contains(className.simpleName)) {
      return;
    }
    ClassName topLevelClassName = className.topLevelClassName();
    String simpleName = topLevelClassName.simpleName();
    ClassName replaced = importableTypes.put(simpleName, topLevelClassName);
    if (replaced != null) {
      importableTypes.put(simpleName, replaced);
    }
  }

  private ClassName resolve(String simpleName) {
    for (int i = typeSpecStack.size() - 1; i >= 0; i--) {
      TypeSpec typeSpec = typeSpecStack.get(i);
      if (typeSpec.nestedTypesSimpleNames.contains(simpleName)) {
        return stackClassName(i, simpleName);
      }
    }

    if (!typeSpecStack.isEmpty() && Objects.equals(typeSpecStack.get(0).name, simpleName)) {
      return ClassName.get(packageName, simpleName);
    }

    ClassName importedType = importedTypes.get(simpleName);
    if (importedType != null) return importedType;

    return null;
  }

  private ClassName stackClassName(int stackDepth, String simpleName) {
    ClassName className = ClassName.get(packageName, typeSpecStack.get(0).name);
    for (int i = 1; i <= stackDepth; i++) {
      className = className.nestedClass(typeSpecStack.get(i).name);
    }
    return className.nestedClass(simpleName);
  }

  private void emitLineBreak() throws IOException {
    if ((javadoc || comment) && trailingNewline) {
      emitIndentation();
      out.append(javadoc ? " *" : "//");
    }
    out.append("\n");
    trailingNewline = true;
    if (statementLine != -1) {
      if (statementLine == 0) {
        indent(2);
      }
      statementLine++;
    }
  }

  private void emitLineContent(String line) throws IOException {
    if (trailingNewline) {
      emitIndentation();
      if (javadoc) {
        out.append(" * ");
      } else if (comment) {
        out.append("// ");
      }
    }
    out.append(line);
    trailingNewline = false;
  }

  public CodeWriter emitAndIndent(String s) throws IOException {
    boolean first = true;
    for (String line : LINE_BREAKING_PATTERN.split(s, -1)) {
      if (!first) {
        emitLineBreak();
      }
      first = false;
      if (line.isEmpty()) continue;
      emitLineContent(line);
    }
    return this;
  }
  private void emitIndentation() throws IOException {
    for (int j = 0; j < indentationManager.getIndentLevel(); j++) {
      out.append(indentationManager.getIndent());
    }
  }

  public Map<String, ClassName> suggestedImports() {
    Map<String, ClassName> result = new LinkedHashMap<>(importableTypes);
    result.keySet().removeAll(referencedNames);
    return result;
  }
}
