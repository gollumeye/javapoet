// Refactored Abstract Builder Class
package com.squareup.javapoet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;

public abstract class AbstractSpecBuilder<T extends AbstractSpecBuilder<T, R>, R> {
    protected final List<AnnotationSpec> annotations = new ArrayList<>();
    protected final List<Modifier> modifiers = new ArrayList<>();
    protected final CodeBlock.Builder javadoc = CodeBlock.builder();

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    public T addJavadoc(String format, Object... args) {
        javadoc.add(format, args);
        return self();
    }

    public T addJavadoc(CodeBlock block) {
        javadoc.add(block);
        return self();
    }

    public T addAnnotation(AnnotationSpec annotationSpec) {
        annotations.add(annotationSpec);
        return self();
    }

    public T addAnnotation(ClassName annotation) {
        return addAnnotation(AnnotationSpec.builder(annotation).build());
    }

    public T addAnnotation(Class<?> annotation) {
        return addAnnotation(ClassName.get(annotation));
    }

    public T addAnnotations(Iterable<AnnotationSpec> annotationSpecs) {
        for (AnnotationSpec annotationSpec : annotationSpecs) {
            annotations.add(annotationSpec);
        }
        return self();
    }

    public T addModifiers(Modifier... modifiers) {
        Collections.addAll(this.modifiers, modifiers);
        return self();
    }

    public T addModifiers(Iterable<Modifier> modifiers) {
        for (Modifier modifier : modifiers) {
            this.modifiers.add(modifier);
        }
        return self();
    }

    protected abstract R build();
}
