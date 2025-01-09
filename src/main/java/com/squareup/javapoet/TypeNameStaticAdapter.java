package com.squareup.javapoet;

import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Type;

public class TypeNameStaticAdapter implements ITypeNameStaticAdapter {
    public static final TypeName VOID = new TypeName("void");

    @Override
    public ArrayTypeName asArray(TypeNameProvider type) {
        return TypeName.asArray((TypeName) type);
    }

    @Override
    public TypeName get(TypeMirror mirror) {
        return TypeName.get(mirror);
    }

    @Override
    public TypeName get(Type type) {
        return TypeName.get(type);
    }

    public TypeName getVoid() {
        return VOID;
    }

    @Override
    public TypeName toTypeName(TypeNameProvider type) {
        return (TypeName) type;
    }
}
