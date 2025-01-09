package com.squareup.javapoet;

public interface TypeNameProvider {
  boolean isAnnotated();
  TypeName withoutAnnotations();
  TypeName box();
  TypeName unbox();
  boolean isPrimitive();
  boolean isBoxedPrimitive();
}
