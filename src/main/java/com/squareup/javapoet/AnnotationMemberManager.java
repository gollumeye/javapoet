package com.squareup.javapoet;

import javax.lang.model.SourceVersion;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public final class AnnotationMemberManager {
    private Map<String, List<CodeBlock>> members = new LinkedHashMap<>();
    private final List<AnnotationValueHandler.ValueHandler> valueHandlers = new ArrayList<>();


    public AnnotationMemberManager(Map<String, List<CodeBlock>> members) {
        this.members = members;

        valueHandlers.add(new AnnotationValueHandler.ClassValueHandler());
        valueHandlers.add(new AnnotationValueHandler.EnumValueHandler());
        valueHandlers.add(new AnnotationValueHandler.StringValueHandler());
        valueHandlers.add(new AnnotationValueHandler.FloatValueHandler());
        valueHandlers.add(new AnnotationValueHandler.LongValueHandler());
        valueHandlers.add(new AnnotationValueHandler.CharacterValueHandler());
        valueHandlers.add(new AnnotationValueHandler.DefaultValueHandler());
    }

    /**
     * Adds a member with a formatted value to the annotation.
     */
    public void addMember(String name, String format, Object... args) {
        addMember(name, CodeBlock.of(format, args));
    }

    public void addMember(String name, CodeBlock codeBlock) {
        validateMemberName(name);
        List<CodeBlock> values = members.computeIfAbsent(name, k -> new ArrayList<>());
        values.add(codeBlock);
    }


    /**
     * Delegates to {@link #addMember(String, String, Object...)}, with parameter {@code format}
     * depending on the given {@code value} object. Falls back to {@code "$L"} literal format if
     * the class of the given {@code value} object is not supported.
     */
    public void addMemberForValue(String memberName, Object value) {
        validateMemberName(memberName);

        for (AnnotationValueHandler.ValueHandler handler : valueHandlers) {
            if (handler.supports(value)) {
                handler.handle(memberName, value, this);
                return;
            }
        }
    }

    /**
     * Returns an immutable map of members.
     * Internally mutable map is still used
     */
    public Map<String, List<CodeBlock>> getMembers() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(members));
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
