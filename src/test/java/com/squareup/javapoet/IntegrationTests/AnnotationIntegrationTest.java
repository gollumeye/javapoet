package com.squareup.javapoet.IntegrationTests;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.junit.Test;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

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
        String expectedOutput = "package com.example;\n\n"
                + "import java.lang.SuppressWarnings;\n\n"
                + "@SuppressWarnings(\"unchecked\")\n"
                + "public class TestClass {\n"
                + "}\n";
        assertEquals(expectedOutput, actualOutput);
    }
}
