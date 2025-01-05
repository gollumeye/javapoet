package com.squareup.javapoet;

import java.io.IOException;

public interface AnnotationFormatter {
    void format(AnnotationSpec annotationSpec, CodeWriter codeWriter, boolean inline) throws IOException;
}
