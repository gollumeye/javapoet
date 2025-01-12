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
import java.util.concurrent.TimeUnit;
import javax.lang.model.element.Modifier;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public final class JavaFileStaticImportsTest {

    @Rule public final CompilationRule compilation = new CompilationRule();

    @Test public void importStaticReadmeExample() {
        ClassName hoverboard = ClassName.get("com.mattel", "Hoverboard");
        ClassName namedBoards = ClassName.get("com.mattel", "Hoverboard", "Boards");
        ClassName list = ClassName.get("java.util", "List");
        ClassName arrayList = ClassName.get("java.util", "ArrayList");
        TypeName listOfHoverboards = ParameterizedTypeName.get(list, hoverboard);
        MethodSpec beyond = MethodSpec.methodBuilder("beyond")
                .returns(listOfHoverboards)
                .addStatement("$T result = new $T<>()", listOfHoverboards, arrayList)
                .addStatement("result.add($T.createNimbus(2000))", hoverboard)
                .addStatement("result.add($T.createNimbus(\"2001\"))", hoverboard)
                .addStatement("result.add($T.createNimbus($T.THUNDERBOLT))", hoverboard, namedBoards)
                .addStatement("$T.sort(result)", Collections.class)
                .addStatement("return result.isEmpty() ? $T.emptyList() : result", Collections.class)
                .build();
        TypeSpec hello = TypeSpec.classBuilder("HelloWorld")
                .addMethod(beyond)
                .build();
        JavaFile example = JavaFile.builder("com.example.helloworld", hello)
                .addStaticImport(hoverboard, "createNimbus")
                .addStaticImport(namedBoards, "*")
                .addStaticImport(Collections.class, "*")
                .build();
        assertThat(example.toString()).isEqualTo(""
                + "package com.example.helloworld;\n"
                + "\n"
                + "import static com.mattel.Hoverboard.Boards.*;\n"
                + "import static com.mattel.Hoverboard.createNimbus;\n"
                + "import static java.util.Collections.*;\n"
                + "\n"
                + "import com.mattel.Hoverboard;\n"
                + "import java.util.ArrayList;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class HelloWorld {\n"
                + "  List<Hoverboard> beyond() {\n"
                + "    List<Hoverboard> result = new ArrayList<>();\n"
                + "    result.add(createNimbus(2000));\n"
                + "    result.add(createNimbus(\"2001\"));\n"
                + "    result.add(createNimbus(THUNDERBOLT));\n"
                + "    sort(result);\n"
                + "    return result.isEmpty() ? emptyList() : result;\n"
                + "  }\n"
                + "}\n");
    }
    @Test public void importStaticForCrazyFormatsWorks() {
        MethodSpec method = MethodSpec.methodBuilder("method").build();
        JavaFile.builder("com.squareup.tacos",
                        TypeSpec.classBuilder("Taco")
                                .addStaticBlock(CodeBlock.builder()
                                        .addStatement("$T", Runtime.class)
                                        .addStatement("$T.a()", Runtime.class)
                                        .addStatement("$T.X", Runtime.class)
                                        .addStatement("$T$T", Runtime.class, Runtime.class)
                                        .addStatement("$T.$T", Runtime.class, Runtime.class)
                                        .addStatement("$1T$1T", Runtime.class)
                                        .addStatement("$1T$2L$1T", Runtime.class, "?")
                                        .addStatement("$1T$2L$2S$1T", Runtime.class, "?")
                                        .addStatement("$1T$2L$2S$1T$3N$1T", Runtime.class, "?", method)
                                        .addStatement("$T$L", Runtime.class, "?")
                                        .addStatement("$T$S", Runtime.class, "?")
                                        .addStatement("$T$N", Runtime.class, method)
                                        .build())
                                .build())
                .addStaticImport(Runtime.class, "*")
                .build()
                .toString(); // don't look at the generated code...
    }

    @Test public void importStaticMixed() {
        JavaFile source = JavaFile.builder("com.squareup.tacos",
                        TypeSpec.classBuilder("Taco")
                                .addStaticBlock(CodeBlock.builder()
                                        .addStatement("assert $1T.valueOf(\"BLOCKED\") == $1T.BLOCKED", Thread.State.class)
                                        .addStatement("$T.gc()", System.class)
                                        .addStatement("$1T.out.println($1T.nanoTime())", System.class)
                                        .build())
                                .addMethod(MethodSpec.constructorBuilder()
                                        .addParameter(Thread.State[].class, "states")
                                        .varargs(true)
                                        .build())
                                .build())
                .addStaticImport(Thread.State.BLOCKED)
                .addStaticImport(System.class, "*")
                .addStaticImport(Thread.State.class, "valueOf")
                .build();
        assertThat(source.toString()).isEqualTo(""
                + "package com.squareup.tacos;\n"
                + "\n"
                + "import static java.lang.System.*;\n"
                + "import static java.lang.Thread.State.BLOCKED;\n"
                + "import static java.lang.Thread.State.valueOf;\n"
                + "\n"
                + "import java.lang.Thread;\n"
                + "\n"
                + "class Taco {\n"
                + "  static {\n"
                + "    assert valueOf(\"BLOCKED\") == BLOCKED;\n"
                + "    gc();\n"
                + "    out.println(nanoTime());\n"
                + "  }\n"
                + "\n"
                + "  Taco(Thread.State... states) {\n"
                + "  }\n"
                + "}\n");
    }

    @Ignore("addStaticImport doesn't support members with $L")
    @Test public void importStaticDynamic() {
        JavaFile source = JavaFile.builder("com.squareup.tacos",
                        TypeSpec.classBuilder("Taco")
                                .addMethod(MethodSpec.methodBuilder("main")
                                        .addStatement("$T.$L.println($S)", System.class, "out", "hello")
                                        .build())
                                .build())
                .addStaticImport(System.class, "out")
                .build();
        assertThat(source.toString()).isEqualTo(""
                + "package com.squareup.tacos;\n"
                + "\n"
                + "import static java.lang.System.out;\n"
                + "\n"
                + "class Taco {\n"
                + "  void main() {\n"
                + "    out.println(\"hello\");\n"
                + "  }\n"
                + "}\n");
    }

    @Test public void importStaticNone() {
        assertThat(JavaFile.builder("readme", importStaticTypeSpec("Util"))
                .build().toString()).isEqualTo(""
                + "package readme;\n"
                + "\n"
                + "import java.lang.System;\n"
                + "import java.util.concurrent.TimeUnit;\n"
                + "\n"
                + "class Util {\n"
                + "  public static long minutesToSeconds(long minutes) {\n"
                + "    System.gc();\n"
                + "    return TimeUnit.SECONDS.convert(minutes, TimeUnit.MINUTES);\n"
                + "  }\n"
                + "}\n");
    }

    @Test public void importStaticOnce() {
        assertThat(JavaFile.builder("readme", importStaticTypeSpec("Util"))
                .addStaticImport(TimeUnit.SECONDS)
                .build().toString()).isEqualTo(""
                + "package readme;\n"
                + "\n"
                + "import static java.util.concurrent.TimeUnit.SECONDS;\n"
                + "\n"
                + "import java.lang.System;\n"
                + "import java.util.concurrent.TimeUnit;\n"
                + "\n"
                + "class Util {\n"
                + "  public static long minutesToSeconds(long minutes) {\n"
                + "    System.gc();\n"
                + "    return SECONDS.convert(minutes, TimeUnit.MINUTES);\n"
                + "  }\n"
                + "}\n");
    }

    @Test public void importStaticTwice() {
        assertThat(JavaFile.builder("readme", importStaticTypeSpec("Util"))
                .addStaticImport(TimeUnit.SECONDS)
                .addStaticImport(TimeUnit.MINUTES)
                .build().toString()).isEqualTo(""
                + "package readme;\n"
                + "\n"
                + "import static java.util.concurrent.TimeUnit.MINUTES;\n"
                + "import static java.util.concurrent.TimeUnit.SECONDS;\n"
                + "\n"
                + "import java.lang.System;\n"
                + "\n"
                + "class Util {\n"
                + "  public static long minutesToSeconds(long minutes) {\n"
                + "    System.gc();\n"
                + "    return SECONDS.convert(minutes, MINUTES);\n"
                + "  }\n"
                + "}\n");
    }

    @Test public void importStaticUsingWildcards() {
        assertThat(JavaFile.builder("readme", importStaticTypeSpec("Util"))
                .addStaticImport(TimeUnit.class, "*")
                .addStaticImport(System.class, "*")
                .build().toString()).isEqualTo(""
                + "package readme;\n"
                + "\n"
                + "import static java.lang.System.*;\n"
                + "import static java.util.concurrent.TimeUnit.*;\n"
                + "\n"
                + "class Util {\n"
                + "  public static long minutesToSeconds(long minutes) {\n"
                + "    gc();\n"
                + "    return SECONDS.convert(minutes, MINUTES);\n"
                + "  }\n"
                + "}\n");
    }

    private TypeSpec importStaticTypeSpec(String name) {
        MethodSpec method = MethodSpec.methodBuilder("minutesToSeconds")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(long.class)
                .addParameter(long.class, "minutes")
                .addStatement("$T.gc()", System.class)
                .addStatement("return $1T.SECONDS.convert(minutes, $1T.MINUTES)", TimeUnit.class)
                .build();
        return TypeSpec.classBuilder(name).addMethod(method).build();

    }

    @Test public void modifyStaticImports() {
        JavaFile.Builder builder = JavaFile.builder("com.squareup.tacos",
                        TypeSpec.classBuilder("Taco")
                                .build())
                .addStaticImport(File.class, "separator");

        builder.staticImports.clear();
        builder.staticImports.add(File.class.getCanonicalName() + ".separatorChar");

        String source = builder.build().toString();

        assertThat(source).isEqualTo(""
                + "package com.squareup.tacos;\n"
                + "\n"
                + "import static java.io.File.separatorChar;\n"
                + "\n"
                + "class Taco {\n"
                + "}\n");
    }
}
