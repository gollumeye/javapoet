import com.squareup.javapoet.*;
import org.junit.Test;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class OwnTest {

    @Test public void systemTest() throws IOException {
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
}
