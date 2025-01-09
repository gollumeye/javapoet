package com.squareup.javapoet.codewriter;

public class IndentationManager {
    private final String indent;
    private int indentLevel = 0;

    IndentationManager(String indent) {
        this.indent = indent;
    }

    void indent() {
        indent(1);
    }

    void indent(int levels) {
        indentLevel += levels;
    }

    void unindent() {
        unindent(1);
    }

    void unindent(int levels) {
        if (indentLevel - levels >= 0) {
            indentLevel -= levels;
        } else {
            throw new IllegalArgumentException("Cannot unindent more than current level");
        }
    }

    int getIndentLevel() {
        return indentLevel;
    }

    String getIndent() {
        return indent;
    }
}
