package com.squareup.javapoet;

import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.Collections;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import javax.lang.model.element.Modifier;

import static com.google.common.truth.Truth.assertThat;

public class CodeWriterTest {

    @Test
    public void emitString() throws IOException {
        StringBuilder out = new StringBuilder();
        CodeWriter writer = new CodeWriter(out);
        writer.emit("Hello, World!");
        assertThat(out.toString()).isEqualTo("Hello, World!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void unindentTooMuch() {
        StringBuilder out = new StringBuilder();
        CodeWriter writer = new CodeWriter(out);
        writer.unindent(1);
    }

    @Test
    public void javadocEmission() throws IOException {
        StringBuilder out = new StringBuilder();
        CodeWriter writer = new CodeWriter(out);
        writer.emitJavadoc(CodeBlock.of("This is a Javadoc.\nWith a second line."));
        assertThat(out.toString()).isEqualTo(
                "/**\n" +
                        " * This is a Javadoc.\n" +
                        " * With a second line.\n" +
                        " */\n");
    }

    @Test
    public void emptyJavadoc() throws IOException {
        StringBuilder out = new StringBuilder();
        CodeWriter writer = new CodeWriter(out);
        writer.emitJavadoc(CodeBlock.builder().build());
        assertThat(out.toString()).isEqualTo("");
    }

    @Test
    public void commentEmission() throws IOException {
        StringBuilder out = new StringBuilder();
        CodeWriter writer = new CodeWriter(out);
        writer.emitComment(CodeBlock.of("This is a comment."));
        assertThat(out.toString()).isEqualTo("// This is a comment.\n");
    }

    @Test
    public void emitModifiers() throws IOException {
        StringBuilder out = new StringBuilder();
        CodeWriter writer = new CodeWriter(out);
        writer.emitModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC));
        assertThat(out.toString()).isEqualTo("public static ");
    }

    @Test
    public void emitTypeVariables() throws IOException {
        StringBuilder out = new StringBuilder();
        CodeWriter writer = new CodeWriter(out);
        TypeVariableName typeVariable = TypeVariableName.get("T", ClassName.get("java.lang", "Number"));
        writer.emitTypeVariables(Collections.singletonList(typeVariable));
        assertThat(out.toString()).isEqualTo("<T extends java.lang.Number>");
    }

    @Test
    public void pushAndPopPackage() {
        StringBuilder out = new StringBuilder();
        CodeWriter writer = new CodeWriter(out);
        writer.pushPackage("com.example");
        assertThat(writer.lookupName(ClassName.get("com.example", "MyClass"))).isEqualTo("MyClass");
        writer.popPackage();
    }

    @Test
    public void staticImportMemberEmission() throws IOException {
        StringBuilder out = new StringBuilder();
        Set<String> staticImports = new HashSet<>(Collections.singletonList("java.util.Collections.emptyList"));
        CodeWriter writer = new CodeWriter(out, "  ", staticImports, Collections.emptySet());
        writer.emit("$T.emptyList()", ClassName.get("java.util", "Collections"));
        assertThat(out.toString()).isEqualTo("emptyList()");
    }

    @Test
    public void emitLiteralTypeSpec() throws IOException {
        StringBuilder out = new StringBuilder();
        CodeWriter writer = new CodeWriter(out);
        TypeSpec typeSpec = TypeSpec.classBuilder("MyClass").build();
        writer.emitLiteral(typeSpec);
        assertThat(out.toString()).contains("class MyClass");
    }

    @Test
    public void emptyLineInJavaDocDosEndings() throws IOException {
        CodeBlock javadocCodeBlock = CodeBlock.of("A\r\n\r\nB\r\n");
        StringBuilder out = new StringBuilder();
        new CodeWriter(out).emitJavadoc(javadocCodeBlock);
        assertThat(out.toString()).isEqualTo(
                "/**\n" +
                        " * A\n" +
                        " *\n" +
                        " * B\n" +
                        " */\n");
    }


    @Test
    public void emitAnnotations() throws IOException {
        AnnotationSpec annotationSpec = AnnotationSpec.builder(Deprecated.class).build();
        StringBuilder out = new StringBuilder();
        CodeWriter writer = new CodeWriter(out);

        writer.emitAnnotations(Collections.singletonList(annotationSpec), false);

        assertThat(out.toString()).isEqualTo("@java.lang.Deprecated\n");
    }

    @Test
    public void emitCodeBlock() throws IOException {
        CodeBlock codeBlock = CodeBlock.of("System.out.println($S);", "Hello, world!");
        StringBuilder out = new StringBuilder();
        CodeWriter writer = new CodeWriter(out);

        writer.emit(codeBlock);

        assertThat(out.toString()).isEqualTo("System.out.println(\"Hello, world!\");");
    }

    @Test
    public void emitJavadocWithIndentation() throws IOException {
        CodeBlock javadocCodeBlock = CodeBlock.of("This is a test.");
        StringBuilder out = new StringBuilder();
        CodeWriter writer = new CodeWriter(out);

        writer.indent();
        writer.emitJavadoc(javadocCodeBlock);

        assertThat(out.toString()).isEqualTo(
                "/**\n" +
                        "   * This is a test.\n" +
                        "   */\n");
    }

    @Test
    public void lookupName() {
        Map<String, ClassName> importedTypes = new HashMap<>();
        importedTypes.put("List", ClassName.get("java.util", "List"));

        StringBuilder out = new StringBuilder();
        CodeWriter writer = new CodeWriter(out, "  ", importedTypes, Collections.emptySet(), Collections.emptySet());

        String name = writer.lookupName(ClassName.get("java.util", "List"));

        assertThat(name).isEqualTo("List");
    }

    @Test
    public void emitLiteralString() throws IOException {
        StringBuilder out = new StringBuilder();
        CodeWriter writer = new CodeWriter(out);

        writer.emit("$S", "Test String");

        assertThat(out.toString()).isEqualTo("\"Test String\"");
    }

    @Test
    public void emitNestedTypeName() {
        TypeSpec outerType = TypeSpec.classBuilder("Outer").build();
        TypeSpec innerType = TypeSpec.classBuilder("Inner").build();

        StringBuilder out = new StringBuilder();
        CodeWriter writer = new CodeWriter(out);

        writer.pushType(outerType);
        writer.pushType(innerType);

        assertThat(writer.lookupName(ClassName.get("com.example", "Outer", "Inner"))).isEqualTo("com.example.Outer.Inner");

        writer.popType();
        writer.popType();
    }
}
