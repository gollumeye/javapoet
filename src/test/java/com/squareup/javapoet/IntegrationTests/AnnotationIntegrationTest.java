package com.squareup.javapoet.IntegrationTests;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.junit.Test;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class AnnotationIntegrationTest {

    @Test
    public void annotationSpecIntegration() throws IOException {
        AnnotationSpec testAnnotation = AnnotationSpec.builder(SuppressWarnings.class)
                .addMember("value", "$S", "unchecked")
                .build();

        TypeSpec testClass = TypeSpec.classBuilder("TestClass")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(testAnnotation)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example", testClass)
                .build();

        StringWriter stringWriter = new StringWriter();
        javaFile.writeTo(stringWriter);
        String actualOutput = stringWriter.toString();
        String expectedOutput = "package com.example;\n\n"
                + "import java.lang.SuppressWarnings;\n\n"
                + "@SuppressWarnings(\"unchecked\")\n"
                + "public class TestClass {\n"
                + "}\n";

        assertEquals(expectedOutput.trim(), actualOutput.trim());
    }

    @Test
    public void annotationWithMultipleMembers() throws IOException {
        AnnotationSpec testAnnotation = AnnotationSpec.builder(Deprecated.class)
                .addMember("since", "$S", "1.0")
                .addMember("forRemoval", "$L", true)
                .build();

        TypeSpec testClass = TypeSpec.classBuilder("DeprecatedClass")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(testAnnotation)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example", testClass)
                .build();

        StringWriter stringWriter = new StringWriter();
        javaFile.writeTo(stringWriter);
        String actualOutput = stringWriter.toString();

        String expectedOutput = "package com.example;\n\n"
                + "import java.lang.Deprecated;\n\n"
                + "@Deprecated(\n"
                + "    since = \"1.0\",\n"
                + "    forRemoval = true\n"
                + ")\n"
                + "public class DeprecatedClass {\n"
                + "}\n";

        assertEquals(expectedOutput.trim(), actualOutput.trim());
    }

    @Test
    public void annotationOnMethod() throws IOException {
        AnnotationSpec testAnnotation = AnnotationSpec.builder(Override.class).build();

        MethodSpec method = MethodSpec.methodBuilder("toString")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addAnnotation(testAnnotation)
                .addStatement("return $S", "TestClass")
                .build();

        TypeSpec testClass = TypeSpec.classBuilder("TestClass")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(method)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example", testClass)
                .build();

        StringWriter stringWriter = new StringWriter();
        javaFile.writeTo(stringWriter);
        String actualOutput = stringWriter.toString();

        String expectedOutput = "package com.example;\n\n"
                + "import java.lang.Override;\n"
                + "import java.lang.String;\n\n"
                + "public class TestClass {\n"
                + "  @Override\n"
                + "  public String toString() {\n"
                + "    return \"TestClass\";\n"
                + "  }\n"
                + "}\n";

        assertEquals(expectedOutput.trim(), actualOutput.trim());
    }

    @Test
    public void annotationOnParameter() throws IOException {
        AnnotationSpec testAnnotation = AnnotationSpec.builder(Deprecated.class).build();

        MethodSpec method = MethodSpec.methodBuilder("setValue")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "value", Modifier.FINAL)
                .addAnnotation(testAnnotation)
                .addStatement("this.value = value")
                .build();

        TypeSpec testClass = TypeSpec.classBuilder("TestClass")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(method)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example", testClass)
                .build();

        StringWriter stringWriter = new StringWriter();
        javaFile.writeTo(stringWriter);
        String actualOutput = stringWriter.toString();

        String expectedOutput = "package com.example;\n\n"
                + "import java.lang.Deprecated;\n"
                + "import java.lang.String;\n\n"
                + "public class TestClass {\n"
                + "  @Deprecated\n"
                + "  public void setValue(final String value) {\n"
                + "    this.value = value;\n"
                + "  }\n"
                + "}\n";

        assertEquals(expectedOutput.trim(), actualOutput.trim());
    }

    @Test
    public void nestedAnnotation() throws IOException {
        AnnotationSpec innerAnnotation = AnnotationSpec.builder(Deprecated.class)
                .addMember("since", "$S", "1.1")
                .addMember("forRemoval", "$L", true)
                .build();

        AnnotationSpec outerAnnotation = AnnotationSpec.builder(SuppressWarnings.class)
                .addMember("value", "$S", "unchecked")
                .addMember("nested", "$L", innerAnnotation)
                .build();

        TypeSpec testClass = TypeSpec.classBuilder("TestClass")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(outerAnnotation)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example", testClass)
                .build();

        StringWriter stringWriter = new StringWriter();
        javaFile.writeTo(stringWriter);
        String actualOutput = stringWriter.toString();

        String expectedOutput = "package com.example;\n\n"
                + "import java.lang.Deprecated;\n"
                + "import java.lang.SuppressWarnings;\n\n"
                + "@SuppressWarnings(\n"
                + "    value = \"unchecked\",\n"
                + "    nested = @Deprecated(since = \"1.1\", forRemoval = true)\n"
                + ")\n"
                + "public class TestClass {\n"
                + "}\n";

        assertEquals(expectedOutput.trim(), actualOutput.trim());
    }
}
