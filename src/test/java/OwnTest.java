import com.squareup.javapoet.*;
import javax.lang.model.element.Modifier;
import java.io.IOException;

public class OwnTest {
    public static void main(String[] args) throws IOException {
        AnnotationSpec methodAnnotation = AnnotationSpec.builder(Override.class).build();

        AnnotationSpec classAnnotation = AnnotationSpec.builder(Deprecated.class)
                .addMember("since", "$S", "1.0")
                .addMember("forRemoval", "$L", true)
                .build();

        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(methodAnnotation) // Add annotation to the method
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello World")
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(classAnnotation) // Add annotation to the class
                .addMethod(main)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
                .build();
        javaFile.writeTo(System.out);
    }
}
