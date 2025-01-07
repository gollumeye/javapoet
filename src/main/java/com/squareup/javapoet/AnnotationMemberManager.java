package com.squareup.javapoet;

import java.util.*;
import javax.lang.model.SourceVersion;

public final class AnnotationMemberManager {
    private Map<String, List<CodeBlock>> members = new LinkedHashMap<>();

    public AnnotationMemberManager(Map<String, List<CodeBlock>> members) {
        this.members = members;
    }

    /**
     * Adds a member with a formatted value to the annotation.
     */
    public void addMember(String name, String format, Object... args) {
        addMember(name, CodeBlock.of(format, args));
    }

    public void addMember(String name, CodeBlock codeBlock) {
        validateMemberName(name);
        System.out.println("Before: " + members);
        List<CodeBlock> values = members.computeIfAbsent(name, k -> new ArrayList<>());
        values.add(codeBlock);
        System.out.println("After: " + members);
    }


    /**
     * Delegates to {@link #addMember(String, String, Object...)}, with parameter {@code format}
     * depending on the given {@code value} object. Falls back to {@code "$L"} literal format if
     * the class of the given {@code value} object is not supported.
     */
    public void addMemberForValue(String memberName, Object value) {
        validateMemberName(memberName);
        if (value instanceof Class<?>) {
            addMember(memberName, "$T.class", value);
        } else if (value instanceof Enum<?>) {
            addMember(memberName, "$T.$L", value.getClass(), ((Enum<?>) value).name());
        } else if (value instanceof String) {
            addMember(memberName, "$S", value);
        } else if (value instanceof Float) {
            addMember(memberName, "$Lf", value);
        } else if (value instanceof Long) {
            addMember(memberName, "$LL", value);
        } else if (value instanceof Character) {
            addMember(memberName, "'$L'", Util.characterLiteralWithoutSingleQuotes((char) value));
        } else {
            addMember(memberName, "$L", value);
        }
    }

    /**
     * Returns an immutable map of members.
     */
    public Map<String, List<CodeBlock>> getMembers() {
        System.out.println("Immutable Map of Members Returned:");
        System.out.println(members);
        Map<String, List<CodeBlock>> immutableMembers = Collections.unmodifiableMap(new LinkedHashMap<>(members));
        return immutableMembers; //return immutable copy, internally mutable map is still used
    }

    /**
     * Validates that the given name is a valid annotation member name.
     */
    private void validateMemberName(String name) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        if (!SourceVersion.isName(name)) {
            throw new IllegalArgumentException("not a valid name: " + name);
        }
    }
}
