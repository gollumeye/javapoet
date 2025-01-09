import com.squareup.javapoet.*;
import org.junit.Test;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class EndToEndTests {

    @Test
    public void systemTest1() throws IOException {
        AnnotationSpec methodAnnotation = AnnotationSpec.builder(Override.class).build();

        AnnotationSpec classAnnotation = AnnotationSpec.builder(Deprecated.class)
                .addMember("since", "$S", "1.0")
                .addMember("forRemoval", "$L", true)
                .build();

        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(methodAnnotation)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello World")
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(classAnnotation)
                .addMethod(main)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
                .build();
        StringWriter stringWriter = new StringWriter();
        javaFile.writeTo(stringWriter);

        String actualOutput = stringWriter.toString();
        String expectedOutput = "package com.example.helloworld;\n\n"
                + "import java.lang.Deprecated;\n"
                + "import java.lang.Override;\n"
                + "import java.lang.String;\n"
                + "import java.lang.System;\n\n"
                + "@Deprecated(\n"
                + "    since = \"1.0\",\n"
                + "    forRemoval = true\n"
                + ")\n"
                + "public final class HelloWorld {\n"
                + "  @Override\n"
                + "  public static void main(String[] args) {\n"
                + "    System.out.println(\"Hello World\");\n"
                + "  }\n"
                + "}\n";

        assertEquals(expectedOutput.trim(), actualOutput.trim());
    }

    @Test
    public void systemTest2() throws IOException {
        AnnotationSpec methodAnnotation = AnnotationSpec.builder(Deprecated.class)
                .addMember("since", "$S", "2.0")
                .addMember("forRemoval", "$L", false)
                .build();

        MethodSpec printMessage = MethodSpec.methodBuilder("printMessage")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(String.class, "message")
                .addStatement("$T.out.println(message)", System.class)
                .build();

        TypeSpec messagePrinter = TypeSpec.classBuilder("MessagePrinter")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(methodAnnotation)
                .addMethod(printMessage)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example.messageprinter", messagePrinter)
                .build();
        StringWriter stringWriter = new StringWriter();
        javaFile.writeTo(stringWriter);

        String actualOutput = stringWriter.toString();
        String expectedOutput = "package com.example.messageprinter;\n\n"
                + "import java.lang.Deprecated;\n"
                + "import java.lang.String;\n"
                + "import java.lang.System;\n\n"
                + "@Deprecated(\n"
                + "    since = \"2.0\",\n"
                + "    forRemoval = false\n"
                + ")\n"
                + "public final class MessagePrinter {\n"
                + "  public void printMessage(String message) {\n"
                + "    System.out.println(message);\n"
                + "  }\n"
                + "}\n";

        assertEquals(expectedOutput.trim(), actualOutput.trim());
    }

    @Test
    public void systemTest3() throws IOException {
        AnnotationSpec classAnnotation = AnnotationSpec.builder(SuppressWarnings.class)
                .addMember("value", "$S", "unchecked")
                .build();

        MethodSpec printNumbers = MethodSpec.methodBuilder("printNumbers")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("for (int i = 0; i < 5; i++) { $T.out.println(i); }", System.class)
                .build();

        TypeSpec numberPrinter = TypeSpec.classBuilder("NumberPrinter")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(classAnnotation)
                .addMethod(printNumbers)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example.numberprinter", numberPrinter)
                .build();
        StringWriter stringWriter = new StringWriter();
        javaFile.writeTo(stringWriter);

        String actualOutput = stringWriter.toString();
        String expectedOutput = "package com.example.numberprinter;\n\n"
                + "import java.lang.SuppressWarnings;\n"
                + "import java.lang.System;\n\n"
                + "@SuppressWarnings(\"unchecked\")\n"
                + "public final class NumberPrinter {\n"
                + "  public void printNumbers() {\n"
                + "    for (int i = 0; i < 5; i++) { System.out.println(i); };\n"
                + "  }\n"
                + "}\n";

        assertEquals(expectedOutput.trim(), actualOutput.trim());
    }

    @Test
    public void systemTest4() throws IOException {
        AnnotationSpec methodAnnotation = AnnotationSpec.builder(SuppressWarnings.class)
                .addMember("value", "$S", "unused")
                .build();

        MethodSpec calculateSum = MethodSpec.methodBuilder("calculateSum")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(methodAnnotation)
                .returns(int.class)
                .addParameter(int.class, "a")
                .addParameter(int.class, "b")
                .addStatement("return a + b")
                .build();

        TypeSpec calculator = TypeSpec.classBuilder("Calculator")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(calculateSum)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example.calculator", calculator)
                .build();
        StringWriter stringWriter = new StringWriter();
        javaFile.writeTo(stringWriter);

        String actualOutput = stringWriter.toString();
        String expectedOutput = "package com.example.calculator;\n\n"
                + "import java.lang.SuppressWarnings;\n\n"
                + "public class Calculator {\n"
                + "  @SuppressWarnings(\"unused\")\n"
                + "  public static int calculateSum(int a, int b) {\n"
                + "    return a + b;\n"
                + "  }\n"
                + "}\n";

        assertEquals(expectedOutput.trim(), actualOutput.trim());
    }
}
