/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.javapoet;

import java.io.File;
import com.google.testing.compile.CompilationRule;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public final class JavaFileTest {

  @Rule public final CompilationRule compilation = new CompilationRule();

  private TypeElement getElement(Class<?> clazz) {
    return compilation.getElements().getTypeElement(clazz.getCanonicalName());
  }

  @Test public void noImports() {
    String source = JavaFile.builder("com.squareup.tacos",
        TypeSpec.classBuilder("Taco").build())
        .build()
        .toString();
    assertThat(source).isEqualTo(""
        + "package com.squareup.tacos;\n"
        + "\n"
        + "class Taco {\n"
        + "}\n");
  }

  @Test public void singleImport() {
    String source = JavaFile.builder("com.squareup.tacos",
        TypeSpec.classBuilder("Taco")
            .addField(Date.class, "madeFreshDate")
            .build())
        .build()
        .toString();
    assertThat(source).isEqualTo(""
        + "package com.squareup.tacos;\n"
        + "\n"
        + "import java.util.Date;\n"
        + "\n"
        + "class Taco {\n"
        + "  Date madeFreshDate;\n"
        + "}\n");
  }

  @Test public void annotatedTypeParam() {
    String source = JavaFile.builder("com.squareup.tacos",
        TypeSpec.classBuilder("Taco")
            .addField(ParameterizedTypeName.get(ClassName.get(List.class),
                ClassName.get("com.squareup.meat", "Chorizo")
                    .annotated(AnnotationSpec.builder(ClassName.get("com.squareup.tacos", "Spicy"))
                        .build())), "chorizo")
            .build())
        .build()
        .toString();
    assertThat(source).isEqualTo(""
        + "package com.squareup.tacos;\n"
        + "\n"
        + "import com.squareup.meat.Chorizo;\n"
        + "import java.util.List;\n"
        + "\n"
        + "class Taco {\n"
        + "  List<@Spicy Chorizo> chorizo;\n"
        + "}\n");
  }

  @Test public void superclassReferencesSelf() {
    String source = JavaFile.builder("com.squareup.tacos",
        TypeSpec.classBuilder("Taco")
            .superclass(ParameterizedTypeName.get(
                ClassName.get(Comparable.class), ClassName.get("com.squareup.tacos", "Taco")))
            .build())
        .build()
        .toString();
    assertThat(source).isEqualTo(""
        + "package com.squareup.tacos;\n"
        + "\n"
        + "import java.lang.Comparable;\n"
        + "\n"
        + "class Taco extends Comparable<Taco> {\n"
        + "}\n");
  }

  /** https://github.com/square/javapoet/issues/366 */
  @Test public void annotationIsNestedClass() {
    String source = JavaFile.builder("com.squareup.tacos",
        TypeSpec.classBuilder("TestComponent")
            .addAnnotation(ClassName.get("dagger", "Component"))
            .addType(TypeSpec.classBuilder("Builder")
                .addAnnotation(ClassName.get("dagger", "Component", "Builder"))
                .build())
            .build())
        .build()
        .toString();
    assertThat(source).isEqualTo(""
        + "package com.squareup.tacos;\n"
        + "\n"
        + "import dagger.Component;\n"
        + "\n"
        + "@Component\n"
        + "class TestComponent {\n"
        + "  @Component.Builder\n"
        + "  class Builder {\n"
        + "  }\n"
        + "}\n");
  }

  @Test public void defaultPackage() {
    String source = JavaFile.builder("",
        TypeSpec.classBuilder("HelloWorld")
            .addMethod(MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String[].class, "args")
                .addCode("$T.out.println($S);\n", System.class, "Hello World!")
                .build())
            .build())
        .build()
        .toString();
    assertThat(source).isEqualTo(""
        + "import java.lang.String;\n"
        + "import java.lang.System;\n"
        + "\n"
        + "class HelloWorld {\n"
        + "  public static void main(String[] args) {\n"
        + "    System.out.println(\"Hello World!\");\n"
        + "  }\n"
        + "}\n");
  }

  @Test public void defaultPackageTypesAreNotImported() {
    String source = JavaFile.builder("hello",
          TypeSpec.classBuilder("World").addSuperinterface(ClassName.get("", "Test")).build())
        .build()
        .toString();
    assertThat(source).isEqualTo(""
        + "package hello;\n"
        + "\n"
        + "class World implements Test {\n"
        + "}\n");
  }

  @Test public void topOfFileComment() {
    String source = JavaFile.builder("com.squareup.tacos",
        TypeSpec.classBuilder("Taco").build())
        .addFileComment("Generated $L by JavaPoet. DO NOT EDIT!", "2015-01-13")
        .build()
        .toString();
    assertThat(source).isEqualTo(""
        + "// Generated 2015-01-13 by JavaPoet. DO NOT EDIT!\n"
        + "package com.squareup.tacos;\n"
        + "\n"
        + "class Taco {\n"
        + "}\n");
  }

  @Test public void emptyLinesInTopOfFileComment() {
    String source = JavaFile.builder("com.squareup.tacos",
        TypeSpec.classBuilder("Taco").build())
        .addFileComment("\nGENERATED FILE:\n\nDO NOT EDIT!\n")
        .build()
        .toString();
    assertThat(source).isEqualTo(""
        + "//\n"
        + "// GENERATED FILE:\n"
        + "//\n"
        + "// DO NOT EDIT!\n"
        + "//\n"
        + "package com.squareup.tacos;\n"
        + "\n"
        + "class Taco {\n"
        + "}\n");
  }

  @Test
  public void alwaysQualifySimple() {
    String source = createSourceWithAlwaysQualifiedField(Thread.class, "thread", true);
    assertThat(source).isEqualTo(""
            + "package com.squareup.tacos;\n"
            + "\n"
            + "class Taco {\n"
            + "  java.lang.Thread thread;\n"
            + "}\n");
  }

  @Test
  public void alwaysQualifySupersedesJavaLangImports() {
    String source = createSourceWithAlwaysQualifiedField(Thread.class, "thread", false);
    assertThat(source).isEqualTo(""
            + "package com.squareup.tacos;\n"
            + "\n"
            + "class Taco {\n"
            + "  java.lang.Thread thread;\n"
            + "}\n");
  }

  private String createSourceWithAlwaysQualifiedField(Class<?> fieldType, String fieldName, boolean skipJavaLangImports) {
    return JavaFile.builder("com.squareup.tacos",
                    TypeSpec.classBuilder("Taco")
                            .addField(fieldType, fieldName)
                            .alwaysQualify(fieldType.getSimpleName())
                            .build())
            .skipJavaLangImports(skipJavaLangImports)
            .build()
            .toString();
  }


  @Test
  public void avoidClashesWithNestedClasses_viaClass() {
    String source = createSourceAvoidingClashes(Foo.class);
    assertThat(source).isEqualTo(""
            + "package com.squareup.tacos;\n"
            + "\n"
            + "import other.Foo;\n"
            + "import other.NestedTypeC;\n"
            + "\n"
            + "class Taco {\n"
            + "  other.NestedTypeA nestedA;\n"
            + "\n"
            + "  other.NestedTypeB nestedB;\n"
            + "\n"
            + "  NestedTypeC nestedC;\n"
            + "\n"
            + "  Foo foo;\n"
            + "}\n");
  }

  @Test
  public void avoidClashesWithNestedClasses_viaTypeElement() {
    String source = createSourceAvoidingClashes(getElement(Foo.class));
    assertThat(source).isEqualTo(""
            + "package com.squareup.tacos;\n"
            + "\n"
            + "import other.Foo;\n"
            + "import other.NestedTypeC;\n"
            + "\n"
            + "class Taco {\n"
            + "  other.NestedTypeA nestedA;\n"
            + "\n"
            + "  other.NestedTypeB nestedB;\n"
            + "\n"
            + "  NestedTypeC nestedC;\n"
            + "\n"
            + "  Foo foo;\n"
            + "}\n");
  }

  /**
   * Helper method to create a source code string while avoiding clashes with nested classes.
   *
   * @param avoidClashTarget The argument for `avoidClashesWithNestedClasses` (can be a class or type element).
   * @return The source code string.
   */
  private String createSourceAvoidingClashes(Object avoidClashTarget) {
    TypeSpec.Builder tacoBuilder = TypeSpec.classBuilder("Taco")
            // These two should get qualified
            .addField(ClassName.get("other", "NestedTypeA"), "nestedA")
            .addField(ClassName.get("other", "NestedTypeB"), "nestedB")
            // This one shouldn't since it's not a nested type of Foo
            .addField(ClassName.get("other", "NestedTypeC"), "nestedC")
            // This one shouldn't since we only look at nested types
            .addField(ClassName.get("other", "Foo"), "foo");

    // Apply avoidClashesWithNestedClasses with the correct argument type
    if (avoidClashTarget instanceof Class) {
      tacoBuilder.avoidClashesWithNestedClasses((Class<?>) avoidClashTarget);
    } else if (avoidClashTarget instanceof javax.lang.model.element.TypeElement) {
      tacoBuilder.avoidClashesWithNestedClasses((javax.lang.model.element.TypeElement) avoidClashTarget);
    }

    return JavaFile.builder("com.squareup.tacos", tacoBuilder.build())
            .build()
            .toString();
  }


  @Test public void avoidClashesWithNestedClasses_viaSuperinterfaceType() {
    String source = JavaFile.builder("com.squareup.tacos",
        TypeSpec.classBuilder("Taco")
            // These two should get qualified
            .addField(ClassName.get("other", "NestedTypeA"), "nestedA")
            .addField(ClassName.get("other", "NestedTypeB"), "nestedB")
            // This one shouldn't since it's not a nested type of Foo
            .addField(ClassName.get("other", "NestedTypeC"), "nestedC")
            // This one shouldn't since we only look at nested types
            .addField(ClassName.get("other", "Foo"), "foo")
            .addType(TypeSpec.classBuilder("NestedTypeA").build())
            .addType(TypeSpec.classBuilder("NestedTypeB").build())
            .addSuperinterface(FooInterface.class)
            .build())
        .build()
        .toString();
    assertThat(source).isEqualTo("package com.squareup.tacos;\n"
        + "\n"
        + "import com.squareup.javapoet.JavaFileTest;\n"
        + "import other.Foo;\n"
        + "import other.NestedTypeC;\n"
        + "\n"
        + "class Taco implements JavaFileTest.FooInterface {\n"
        + "  other.NestedTypeA nestedA;\n"
        + "\n"
        + "  other.NestedTypeB nestedB;\n"
        + "\n"
        + "  NestedTypeC nestedC;\n"
        + "\n"
        + "  Foo foo;\n"
        + "\n"
        + "  class NestedTypeA {\n"
        + "  }\n"
        + "\n"
        + "  class NestedTypeB {\n"
        + "  }\n"
        + "}\n");
  }

  static class Foo {
    static class NestedTypeA {

    }
    static class NestedTypeB {

    }
  }

  interface FooInterface {
    class NestedTypeA {

    }
    class NestedTypeB {

    }
  }

  private TypeSpec.Builder childTypeBuilder() {
    return TypeSpec.classBuilder("Child")
        .addMethod(MethodSpec.methodBuilder("optionalString")
            .returns(ParameterizedTypeName.get(Optional.class, String.class))
            .addStatement("return $T.empty()", Optional.class)
            .build())
        .addMethod(MethodSpec.methodBuilder("pattern")
            .returns(Pattern.class)
            .addStatement("return null")
            .build());
  }

  private String generateSourceWithSuperclass(Class<?> superclass) {
    return JavaFile.builder("com.squareup.javapoet",
                    childTypeBuilder().superclass(superclass).build())
            .build()
            .toString();
  }

  private String generateSourceWithSuperclass(TypeMirror superclassTypeMirror) {
    return JavaFile.builder("com.squareup.javapoet",
                    childTypeBuilder().superclass(superclassTypeMirror).build())
            .build()
            .toString();
  }

  private String generateSourceWithSuperinterface(Class<?> superinterface) {
    return JavaFile.builder("com.squareup.javapoet",
                    childTypeBuilder().addSuperinterface(superinterface).build())
            .build()
            .toString();
  }

  private String generateSourceWithSuperinterface(TypeMirror superinterfaceTypeMirror) {
    return JavaFile.builder("com.squareup.javapoet",
                    childTypeBuilder().addSuperinterface(superinterfaceTypeMirror).build())
            .build()
            .toString();
  }

  private void assertGeneratedSource(String source, String expectedSource) {
    assertThat(source).isEqualTo(expectedSource);
  }

  private String expectedSuperclassSource() {
    return "package com.squareup.javapoet;\n"
            + "\n"
            + "import java.lang.String;\n"
            + "\n"
            + "class Child extends JavaFileTest.Parent {\n"
            + "  java.util.Optional<String> optionalString() {\n"
            + "    return java.util.Optional.empty();\n"
            + "  }\n"
            + "\n"
            + "  java.util.regex.Pattern pattern() {\n"
            + "    return null;\n"
            + "  }\n"
            + "}\n";
  }

  private String expectedSuperinterfaceSource() {
    return "package com.squareup.javapoet;\n"
            + "\n"
            + "import java.lang.String;\n"
            + "import java.util.regex.Pattern;\n"
            + "\n"
            + "class Child implements JavaFileTest.ParentInterface {\n"
            + "  java.util.Optional<String> optionalString() {\n"
            + "    return java.util.Optional.empty();\n"
            + "  }\n"
            + "\n"
            + "  Pattern pattern() {\n"
            + "    return null;\n"
            + "  }\n"
            + "}\n";
  }

  @Test
  public void avoidClashes_parentChild_superclass_type() {
    String source = generateSourceWithSuperclass(Parent.class);
    assertGeneratedSource(source, expectedSuperclassSource());
  }

  @Test
  public void avoidClashes_parentChild_superclass_typeMirror() {
    String source = generateSourceWithSuperclass(getElement(Parent.class).asType());
    assertGeneratedSource(source, expectedSuperclassSource());
  }

  @Test
  public void avoidClashes_parentChild_superinterface_type() {
    String source = generateSourceWithSuperinterface(ParentInterface.class);
    assertGeneratedSource(source, expectedSuperinterfaceSource());
  }

  @Test
  public void avoidClashes_parentChild_superinterface_typeMirror() {
    String source = generateSourceWithSuperinterface(getElement(ParentInterface.class).asType());
    assertGeneratedSource(source, expectedSuperinterfaceSource());
  }


  // Regression test for https://github.com/square/javapoet/issues/77
  // This covers class and inheritance
  static class Parent implements ParentInterface {
    static class Pattern {

    }
  }

  interface ParentInterface {
    class Optional {

    }
  }

  // Regression test for case raised here: https://github.com/square/javapoet/issues/77#issuecomment-519972404
  @Test
  public void avoidClashes_mapEntry() {
    String source = JavaFile.builder("com.squareup.javapoet",
        TypeSpec.classBuilder("MapType")
            .addMethod(MethodSpec.methodBuilder("optionalString")
                .returns(ClassName.get("com.foo", "Entry"))
                .addStatement("return null")
                .build())
            .addSuperinterface(Map.class)
            .build())
        .build()
        .toString();
    assertThat(source).isEqualTo("package com.squareup.javapoet;\n"
        + "\n"
        + "import java.util.Map;\n"
        + "\n"
        + "class MapType implements Map {\n"
        + "  com.foo.Entry optionalString() {\n"
        + "    return null;\n"
        + "  }\n"
        + "}\n");
  }
}
