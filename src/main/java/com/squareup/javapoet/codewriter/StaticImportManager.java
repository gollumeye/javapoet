package com.squareup.javapoet.codewriter;

import javax.lang.model.SourceVersion;
import java.util.LinkedHashSet;
import java.util.Set;

public class StaticImportManager {
    private final Set<String> staticImports;
    private final Set<String> staticImportClassNames;

    StaticImportManager(Set<String> staticImports) {
        this.staticImports = staticImports;
        this.staticImportClassNames = extractStaticImportClassNames(staticImports);
    }

    private Set<String> extractStaticImportClassNames(Set<String> staticImports) {
        Set<String> classNames = new LinkedHashSet<>();
        for (String signature : staticImports) {
            classNames.add(signature.substring(0, signature.lastIndexOf('.')));
        }
        return classNames;
    }

    Set<String> getStaticImportClassNames() {
        return staticImportClassNames;
    }

    boolean contains(String canonical, String part) {
        String partWithoutLeadingDot = part.substring(1);
        if (partWithoutLeadingDot.isEmpty()) return false;
        char first = partWithoutLeadingDot.charAt(0);
        if (!Character.isJavaIdentifierStart(first)) return false;
        String explicit = canonical + "." + extractMemberName(partWithoutLeadingDot);
        String wildcard = canonical + ".*";
        return staticImports.contains(explicit) || staticImports.contains(wildcard);
    }

    private String extractMemberName(String part) {
        for (int i = 1; i <= part.length(); i++) {
            if (!SourceVersion.isIdentifier(part.substring(0, i))) {
                return part.substring(0, i - 1);
            }
        }
        return part;
    }
}
