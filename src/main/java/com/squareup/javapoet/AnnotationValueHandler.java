package com.squareup.javapoet;

public class AnnotationValueHandler {
    public interface ValueHandler {
        boolean supports(Object value);
        void handle(String memberName, Object value, AnnotationMemberManager manager);
    }

    public static class ClassValueHandler implements ValueHandler {
        @Override
        public boolean supports(Object value) {
            return value instanceof Class;
        }

        @Override
        public void handle(String memberName, Object value, AnnotationMemberManager manager) {
            manager.addMember(memberName, "$T.class", value);
        }
    }

    public static class EnumValueHandler implements ValueHandler {
        @Override
        public boolean supports(Object value) {
            return value instanceof Enum;
        }

        @Override
        public void handle(String memberName, Object value, AnnotationMemberManager manager) {
            manager.addMember(memberName, "$T.$L", value.getClass(), ((Enum<?>) value).name());
        }
    }

    public static class StringValueHandler implements ValueHandler {
        @Override
        public boolean supports(Object value) {
            return value instanceof String;
        }

        @Override
        public void handle(String memberName, Object value, AnnotationMemberManager manager) {
            manager.addMember(memberName, "$S", value);
        }
    }

    public static class FloatValueHandler implements ValueHandler {
        @Override
        public boolean supports(Object value) {
            return value instanceof Float;
        }

        @Override
        public void handle(String memberName, Object value, AnnotationMemberManager manager) {
            manager.addMember(memberName, "$Lf", value);
        }
    }

    public static class LongValueHandler implements ValueHandler {
        @Override
        public boolean supports(Object value) {
            return value instanceof Long;
        }

        @Override
        public void handle(String memberName, Object value, AnnotationMemberManager manager) {
            manager.addMember(memberName, "$LL", value);
        }
    }

    public static class CharacterValueHandler implements ValueHandler {
        @Override
        public boolean supports(Object value) {
            return value instanceof Character;
        }

        @Override
        public void handle(String memberName, Object value, AnnotationMemberManager manager) {
            manager.addMember(memberName, "'$L'", Util.characterLiteralWithoutSingleQuotes((char) value));
        }
    }

    public static class DefaultValueHandler implements ValueHandler {
        @Override
        public boolean supports(Object value) {
            return true;
        }

        @Override
        public void handle(String memberName, Object value, AnnotationMemberManager manager) {
            manager.addMember(memberName, "$L", value);
        }
    }
}

