package com.squareup.javapoet;

import com.squareup.javapoet.codewriter.CodeWriter;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DefaultAnnotationFormatter implements AnnotationFormatter {
    @Override
    public void format(AnnotationSpec annotationSpec, CodeWriter codeWriter, boolean inline)
            throws IOException {
        String whitespace = inline ? "" : "\n";
        String memberSeparator = inline ? ", " : ",\n";

        if (annotationSpec.members.isEmpty()) {
            codeWriter.emit("@$T", annotationSpec.type);  // No members
        } else if (annotationSpec.members.size() == 1
                && annotationSpec.members.containsKey(AnnotationSpec.VALUE)) {
            codeWriter.emit("@$T(", annotationSpec.type);  // Single "value" member
            emitAnnotationValues(codeWriter, whitespace, memberSeparator,
                    annotationSpec.members.get(AnnotationSpec.VALUE));
            codeWriter.emit(")");
        } else {
            codeWriter.emit("@$T(" + whitespace, annotationSpec.type);  // Multiple members
            codeWriter.indent(2);
            for (Iterator<Map.Entry<String, List<CodeBlock>>> i
                 = annotationSpec.members.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry<String, List<CodeBlock>> entry = i.next();
                codeWriter.emit("$L = ", entry.getKey());
                emitAnnotationValues(codeWriter, whitespace, memberSeparator, entry.getValue());
                if (i.hasNext()) {
                    codeWriter.emit(memberSeparator);
                }
            }
            codeWriter.unindent(2);
            codeWriter.emit(whitespace + ")");
        }
    }

    private void emitAnnotationValues(CodeWriter codeWriter, String whitespace,
                                String memberSeparator, List<CodeBlock> values) throws IOException {
        if (values.size() == 1) {
            codeWriter.indent(2);
            codeWriter.emit(values.get(0));  // Single value
            codeWriter.unindent(2);
        } else {
            codeWriter.emit("{" + whitespace);  // Multiple values
            codeWriter.indent(2);
            boolean first = true;
            for (CodeBlock codeBlock : values) {
                if (!first) {
                    codeWriter.emit(memberSeparator);
                }
                codeWriter.emit(codeBlock);
                first = false;
            }
            codeWriter.unindent(2);
            codeWriter.emit(whitespace + "}");
        }
    }
}
