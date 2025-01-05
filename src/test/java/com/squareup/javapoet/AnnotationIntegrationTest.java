package com.squareup.javapoet;
import javax.lang.model.element.Modifier;
import java.io.IOException;

public class AnnotationIntegrationTest {

    //Test the integration of AnnotationSpec with TypeSpec and JavaFile
    public static void main(String[] args) throws IOException {
        AnnotationSpec testAnnotation = AnnotationSpec.builder(SuppressWarnings.class)
                .addMember("value", "$S", "unchecked")
                .build();

        TypeSpec testClass = TypeSpec.classBuilder("TestClass")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(testAnnotation)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example", testClass)
                .build();

        javaFile.writeTo(System.out);
    }
}
