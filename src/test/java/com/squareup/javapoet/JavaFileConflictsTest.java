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

import com.google.testing.compile.CompilationRule;
import java.util.Date;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public final class JavaFileConflictsTest {

    @Rule public final CompilationRule compilation = new CompilationRule();

    @Test public void conflictingImports() {
        String source = JavaFile.builder("com.squareup.tacos",
                        TypeSpec.classBuilder("Taco")
                                .addField(Date.class, "madeFreshDate")
                                .addField(ClassName.get("java.sql", "Date"), "madeFreshDatabaseDate")
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
                + "\n"
                + "  java.sql.Date madeFreshDatabaseDate;\n"
                + "}\n");
    }

    @Test public void skipJavaLangImportsWithConflictingClassLast() {
        // Whatever is used first wins! In this case the Float in java.lang is imported.
        String source = JavaFile.builder("com.squareup.tacos",
                        TypeSpec.classBuilder("Taco")
                                .addField(ClassName.get("java.lang", "Float"), "litres")
                                .addField(ClassName.get("com.squareup.soda", "Float"), "beverage")
                                .build())
                .skipJavaLangImports(true)
                .build()
                .toString();
        assertThat(source).isEqualTo(""
                + "package com.squareup.tacos;\n"
                + "\n"
                + "class Taco {\n"
                + "  Float litres;\n"
                + "\n"
                + "  com.squareup.soda.Float beverage;\n" // Second 'Float' is fully qualified.
                + "}\n");
    }

    @Test public void skipJavaLangImportsWithConflictingClassFirst() {
        // Whatever is used first wins! In this case the Float in com.squareup.soda is imported.
        String source = JavaFile.builder("com.squareup.tacos",
                        TypeSpec.classBuilder("Taco")
                                .addField(ClassName.get("com.squareup.soda", "Float"), "beverage")
                                .addField(ClassName.get("java.lang", "Float"), "litres")
                                .build())
                .skipJavaLangImports(true)
                .build()
                .toString();
        assertThat(source).isEqualTo(""
                + "package com.squareup.tacos;\n"
                + "\n"
                + "import com.squareup.soda.Float;\n"
                + "\n"
                + "class Taco {\n"
                + "  Float beverage;\n"
                + "\n"
                + "  java.lang.Float litres;\n" // Second 'Float' is fully qualified.
                + "}\n");
    }

    @Test public void conflictingParentName() {
        String source = JavaFile.builder("com.squareup.tacos",
                        TypeSpec.classBuilder("A")
                                .addType(TypeSpec.classBuilder("B")
                                        .addType(TypeSpec.classBuilder("Twin").build())
                                        .addType(TypeSpec.classBuilder("C")
                                                .addField(ClassName.get("com.squareup.tacos", "A", "Twin", "D"), "d")
                                                .build())
                                        .build())
                                .addType(TypeSpec.classBuilder("Twin")
                                        .addType(TypeSpec.classBuilder("D")
                                                .build())
                                        .build())
                                .build())
                .build()
                .toString();
        assertThat(source).isEqualTo(""
                + "package com.squareup.tacos;\n"
                + "\n"
                + "class A {\n"
                + "  class B {\n"
                + "    class Twin {\n"
                + "    }\n"
                + "\n"
                + "    class C {\n"
                + "      A.Twin.D d;\n"
                + "    }\n"
                + "  }\n"
                + "\n"
                + "  class Twin {\n"
                + "    class D {\n"
                + "    }\n"
                + "  }\n"
                + "}\n");
    }

    @Test public void conflictingChildName() {
        String source = JavaFile.builder("com.squareup.tacos",
                        TypeSpec.classBuilder("A")
                                .addType(TypeSpec.classBuilder("B")
                                        .addType(TypeSpec.classBuilder("C")
                                                .addField(ClassName.get("com.squareup.tacos", "A", "Twin", "D"), "d")
                                                .addType(TypeSpec.classBuilder("Twin").build())
                                                .build())
                                        .build())
                                .addType(TypeSpec.classBuilder("Twin")
                                        .addType(TypeSpec.classBuilder("D")
                                                .build())
                                        .build())
                                .build())
                .build()
                .toString();
        assertThat(source).isEqualTo(""
                + "package com.squareup.tacos;\n"
                + "\n"
                + "class A {\n"
                + "  class B {\n"
                + "    class C {\n"
                + "      A.Twin.D d;\n"
                + "\n"
                + "      class Twin {\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "\n"
                + "  class Twin {\n"
                + "    class D {\n"
                + "    }\n"
                + "  }\n"
                + "}\n");
    }

    @Test public void conflictingNameOutOfScope() {
        String source = JavaFile.builder("com.squareup.tacos",
                        TypeSpec.classBuilder("A")
                                .addType(TypeSpec.classBuilder("B")
                                        .addType(TypeSpec.classBuilder("C")
                                                .addField(ClassName.get("com.squareup.tacos", "A", "Twin", "D"), "d")
                                                .addType(TypeSpec.classBuilder("Nested")
                                                        .addType(TypeSpec.classBuilder("Twin").build())
                                                        .build())
                                                .build())
                                        .build())
                                .addType(TypeSpec.classBuilder("Twin")
                                        .addType(TypeSpec.classBuilder("D")
                                                .build())
                                        .build())
                                .build())
                .build()
                .toString();
        assertThat(source).isEqualTo(""
                + "package com.squareup.tacos;\n"
                + "\n"
                + "class A {\n"
                + "  class B {\n"
                + "    class C {\n"
                + "      Twin.D d;\n"
                + "\n"
                + "      class Nested {\n"
                + "        class Twin {\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "\n"
                + "  class Twin {\n"
                + "    class D {\n"
                + "    }\n"
                + "  }\n"
                + "}\n");
    }

    @Test public void nestedClassAndSuperclassShareName() {
        String source = JavaFile.builder("com.squareup.tacos",
                        TypeSpec.classBuilder("Taco")
                                .superclass(ClassName.get("com.squareup.wire", "Message"))
                                .addType(TypeSpec.classBuilder("Builder")
                                        .superclass(ClassName.get("com.squareup.wire", "Message", "Builder"))
                                        .build())
                                .build())
                .build()
                .toString();
        assertThat(source).isEqualTo(""
                + "package com.squareup.tacos;\n"
                + "\n"
                + "import com.squareup.wire.Message;\n"
                + "\n"
                + "class Taco extends Message {\n"
                + "  class Builder extends Message.Builder {\n"
                + "  }\n"
                + "}\n");
    }

    @Test public void classAndSuperclassShareName() {
        String source = JavaFile.builder("com.squareup.tacos",
                        TypeSpec.classBuilder("Taco")
                                .superclass(ClassName.get("com.taco.bell", "Taco"))
                                .build())
                .build()
                .toString();
        assertThat(source).isEqualTo(""
                + "package com.squareup.tacos;\n"
                + "\n"
                + "class Taco extends com.taco.bell.Taco {\n"
                + "}\n");
    }

    @Test public void conflictingAnnotation() {
        String source = JavaFile.builder("com.squareup.tacos",
                        TypeSpec.classBuilder("Taco")
                                .addAnnotation(ClassName.get("com.taco.bell", "Taco"))
                                .build())
                .build()
                .toString();
        assertThat(source).isEqualTo(""
                + "package com.squareup.tacos;\n"
                + "\n"
                + "@com.taco.bell.Taco\n"
                + "class Taco {\n"
                + "}\n");
    }

    @Test public void conflictingAnnotationReferencedClass() {
        String source = JavaFile.builder("com.squareup.tacos",
                        TypeSpec.classBuilder("Taco")
                                .addAnnotation(AnnotationSpec.builder(ClassName.get("com.squareup.tacos", "MyAnno"))
                                        .addMember("value", "$T.class", ClassName.get("com.taco.bell", "Taco"))
                                        .build())
                                .build())
                .build()
                .toString();
        assertThat(source).isEqualTo(""
                + "package com.squareup.tacos;\n"
                + "\n"
                + "@MyAnno(com.taco.bell.Taco.class)\n"
                + "class Taco {\n"
                + "}\n");
    }

    @Test public void conflictingTypeVariableBound() {
        String source = JavaFile.builder("com.squareup.tacos",
                        TypeSpec.classBuilder("Taco")
                                .addTypeVariable(
                                        TypeVariableName.get("T", ClassName.get("com.taco.bell", "Taco")))
                                .build())
                .build()
                .toString();
        assertThat(source).isEqualTo(""
                + "package com.squareup.tacos;\n"
                + "\n"
                + "class Taco<T extends com.taco.bell.Taco> {\n"
                + "}\n");
    }

    @Test public void packageClassConflictsWithNestedClass() {
        String source = JavaFile.builder("com.squareup.tacos",
                        TypeSpec.classBuilder("Taco")
                                .addField(ClassName.get("com.squareup.tacos", "A"), "a")
                                .addType(TypeSpec.classBuilder("A").build())
                                .build())
                .build()
                .toString();
        assertThat(source).isEqualTo(""
                + "package com.squareup.tacos;\n"
                + "\n"
                + "class Taco {\n"
                + "  com.squareup.tacos.A a;\n"
                + "\n"
                + "  class A {\n"
                + "  }\n"
                + "}\n");
    }

    @Test public void packageClassConflictsWithSuperlass() {
        String source = JavaFile.builder("com.squareup.tacos",
                        TypeSpec.classBuilder("Taco")
                                .superclass(ClassName.get("com.taco.bell", "A"))
                                .addField(ClassName.get("com.squareup.tacos", "A"), "a")
                                .build())
                .build()
                .toString();
        assertThat(source).isEqualTo(""
                + "package com.squareup.tacos;\n"
                + "\n"
                + "class Taco extends com.taco.bell.A {\n"
                + "  A a;\n"
                + "}\n");
    }
}
