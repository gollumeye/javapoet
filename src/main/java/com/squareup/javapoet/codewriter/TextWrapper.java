package com.squareup.javapoet.codewriter;

import java.io.IOException;

public class TextWrapper {
    private final Appendable out;
    private final String indent;
    private final int maxLength;
    private int column = 0;

    TextWrapper(Appendable out, String indent, int maxLength) {
        this.out = out;
        this.indent = indent;
        this.maxLength = maxLength;
    }

    void append(CharSequence csq) throws IOException {
        out.append(csq);
        column += csq.length();
    }

    void wrappingSpace(int indentLevel) throws IOException {
        if (column > maxLength) {
            out.append("\n");
            for (int i = 0; i < indentLevel; i++) {
                out.append(indent);
            }
            column = indentLevel * indent.length();
        } else {
            out.append(" ");
            column++;
        }
    }

    void zeroWidthSpace(int indentLevel) throws IOException {
        if (column > maxLength) {
            out.append("\n");
            for (int i = 0; i < indentLevel; i++) {
                out.append(indent);
            }
            column = indentLevel * indent.length();
        }
    }

    char lastChar() throws IOException {
        return (char) out.toString().charAt(out.toString().length() - 1);
    }
}
