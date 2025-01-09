package com.squareup.javapoet;

import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Type;

public interface ITypeNameStaticAdapter {
    ArrayTypeName asArray(TypeNameProvider type);
    TypeName get(TypeMirror mirror);
    TypeName get(Type type);
    TypeNameProvider getVoid();
    TypeName toTypeName(TypeNameProvider type);
}
