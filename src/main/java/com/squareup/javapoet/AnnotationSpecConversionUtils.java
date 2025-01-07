package com.squareup.javapoet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;


public final class AnnotationSpecConversionUtils {

    private AnnotationSpecConversionUtils() {
    }

    /**
     * Converts a runtime Annotation to an AnnotationSpec.
     */
    public static AnnotationSpec fromAnnotation(Annotation annotation, boolean includeDefaultValues) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(annotation.annotationType());
        try {
            Method[] methods = annotation.annotationType().getDeclaredMethods();
            Arrays.sort(methods, Comparator.comparing(Method::getName));
            for (Method method : methods) {
                Object value = method.invoke(annotation);
                if (!includeDefaultValues && Objects.deepEquals(value, method.getDefaultValue())) {
                    continue;
                }
                if (value.getClass().isArray()) {
                    for (int i = 0; i < Array.getLength(value); i++) {
                        builder.addMemberForValue(method.getName(), Array.get(value, i));
                    }
                    continue;
                }
                if (value instanceof Annotation) {
                    builder.addMember(method.getName(), "$L", fromAnnotation((Annotation) value, includeDefaultValues));
                    continue;
                }
                builder.addMemberForValue(method.getName(), value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Reflecting " + annotation + " failed!", e);
        }
        return builder.build();
    }

    /**
     * Converts an AnnotationMirror to an AnnotationSpec.
     */
    public static AnnotationSpec fromAnnotationMirror(AnnotationMirror annotationMirror) {
        TypeElement element = (TypeElement) annotationMirror.getAnnotationType().asElement();
        AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.get(element));
        Visitor visitor = new Visitor(builder);
        for (ExecutableElement executableElement : annotationMirror.getElementValues().keySet()) {
            String name = executableElement.getSimpleName().toString();
            AnnotationValue value = annotationMirror.getElementValues().get(executableElement);
            value.accept(visitor, name);
        }
        return builder.build();
    }

    /**
     * Annotation value visitor adding members to the given builder instance.
     */
    private static class Visitor extends SimpleAnnotationValueVisitor8<AnnotationSpec.Builder, String> {
        private final AnnotationSpec.Builder builder;

        Visitor(AnnotationSpec.Builder builder) {
            this.builder = builder;
        }

        @Override
        protected AnnotationSpec.Builder defaultAction(Object o, String name) {
            return builder.addMemberForValue(name, o);
        }

        @Override
        public AnnotationSpec.Builder visitAnnotation(AnnotationMirror a, String name) {
            return builder.addMember(name, "$L", fromAnnotationMirror(a));
        }

        @Override
        public AnnotationSpec.Builder visitEnumConstant(VariableElement c, String name) {
            return builder.addMember(name, "$T.$L", c.asType(), c.getSimpleName());
        }

        @Override
        public AnnotationSpec.Builder visitType(javax.lang.model.type.TypeMirror t, String name) {
            return builder.addMember(name, "$T.class", t);
        }

        @Override
        public AnnotationSpec.Builder visitArray(List<? extends AnnotationValue> values, String name) {
            for (AnnotationValue value : values) {
                value.accept(this, name);
            }
            return builder;
        }
    }
}
