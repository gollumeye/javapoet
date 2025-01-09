package com.squareup.javapoet;

import com.squareup.javapoet.codewriter.CodeWriter;

import java.io.IOException;

public interface TypeNameProvider {
  boolean isAnnotated();
  TypeName withoutAnnotations();
  TypeName box();
  TypeName unbox();
  boolean isPrimitive();
  boolean isBoxedPrimitive();
  CodeWriter emit(CodeWriter out) throws IOException;
}
