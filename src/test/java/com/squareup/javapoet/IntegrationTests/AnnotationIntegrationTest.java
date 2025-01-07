package com.squareup.javapoet.IntegrationTests;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.junit.Test;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.StringWriter;

public class AnnotationIntegrationTest {

    //Test the integration of AnnotationSpec with TypeSpec and JavaFile
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

    }
}
