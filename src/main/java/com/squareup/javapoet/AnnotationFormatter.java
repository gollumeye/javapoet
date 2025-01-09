package com.squareup.javapoet;

import com.squareup.javapoet.codewriter.CodeWriter;

import java.io.IOException;

public interface AnnotationFormatter {
    void format(AnnotationSpec annotationSpec,
                CodeWriter codeWriter,
                boolean inline) throws IOException;
}
