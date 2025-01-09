package com.squareup.javapoet;

import com.squareup.javapoet.codewriter.CodeWriter;
import org.junit.Test;

import javax.lang.model.type.WildcardType;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WildcardTypeNameTest {

    @Test
    public void testSubtypeOf_withTypeName() {
        TypeName upperBound = TypeName.get(String.class);
        WildcardTypeName wildcard = WildcardTypeName.subtypeOf(upperBound);

        assertEquals(1, wildcard.upperBounds.size());
        assertEquals(upperBound, wildcard.upperBounds.get(0));
        assertTrue(wildcard.lowerBounds.isEmpty());
    }

    @Test
    public void testSubtypeOf_withType() {
        WildcardTypeName wildcard = WildcardTypeName.subtypeOf(String.class);

        assertEquals(1, wildcard.upperBounds.size());
        assertEquals(TypeName.get(String.class), wildcard.upperBounds.get(0));
        assertTrue(wildcard.lowerBounds.isEmpty());
    }

    @Test
    public void testSupertypeOf_withTypeName() {
        TypeName lowerBound = TypeName.get(String.class);
        WildcardTypeName wildcard = WildcardTypeName.supertypeOf(lowerBound);

        assertEquals(1, wildcard.lowerBounds.size());
        assertEquals(lowerBound, wildcard.lowerBounds.get(0));
        assertEquals(1, wildcard.upperBounds.size());
        assertEquals(TypeName.OBJECT, wildcard.upperBounds.get(0));
    }

    @Test
    public void testSupertypeOf_withType() {
        WildcardTypeName wildcard = WildcardTypeName.supertypeOf(String.class);

        assertEquals(1, wildcard.lowerBounds.size());
        assertEquals(TypeName.get(String.class), wildcard.lowerBounds.get(0));
        assertEquals(1, wildcard.upperBounds.size());
        assertEquals(TypeName.OBJECT, wildcard.upperBounds.get(0));
    }

    @Test
    public void testAnnotated() {
        WildcardTypeName wildcard = WildcardTypeName.subtypeOf(String.class);
        AnnotationSpec annotation = AnnotationSpec.builder(Deprecated.class).build();

        WildcardTypeName annotatedWildcard = wildcard.annotated(Collections.singletonList(annotation));

        assertEquals(wildcard.upperBounds, annotatedWildcard.upperBounds);
        assertEquals(wildcard.lowerBounds, annotatedWildcard.lowerBounds);
        assertEquals(1, annotatedWildcard.annotations.size());
        assertEquals(annotation, annotatedWildcard.annotations.get(0));
    }

    @Test
    public void testWithoutAnnotations() {
        WildcardTypeName wildcard = WildcardTypeName.subtypeOf(String.class)
                .annotated(Collections.singletonList(AnnotationSpec.builder(Deprecated.class).build()));

        WildcardTypeName withoutAnnotations = (WildcardTypeName) wildcard.withoutAnnotations();

        assertTrue(withoutAnnotations.annotations.isEmpty());
        assertEquals(wildcard.upperBounds, withoutAnnotations.upperBounds);
        assertEquals(wildcard.lowerBounds, withoutAnnotations.lowerBounds);
    }


    @Test
    public void testGet_withWildcardType_noBounds() {
        WildcardType mirror = mock(WildcardType.class);

        when(mirror.getExtendsBound()).thenReturn(null);
        when(mirror.getSuperBound()).thenReturn(null);

        TypeName result = WildcardTypeName.get(mirror);

        assertTrue(result instanceof WildcardTypeName);
        WildcardTypeName wildcard = (WildcardTypeName) result;
        assertEquals(TypeName.OBJECT, wildcard.upperBounds.get(0));
        assertTrue(wildcard.lowerBounds.isEmpty());
    }

    @Test
    public void testEmit_withLowerBound() throws IOException {
        StringBuilder output = new StringBuilder();
        CodeWriter codeWriter = new CodeWriter(output);
        WildcardTypeName wildcard = WildcardTypeName.supertypeOf(String.class);

        wildcard.emit(codeWriter);

        String result = output.toString();
        assertEquals("? super java.lang.String", result);
    }

    @Test
    public void testEmit_withUpperBound() throws IOException {
        // Prepare a CodeWriter with a StringBuilder
        StringBuilder output = new StringBuilder();
        CodeWriter codeWriter = new CodeWriter(output);
        WildcardTypeName wildcard = WildcardTypeName.subtypeOf(Number.class);

        wildcard.emit(codeWriter);

        String result = output.toString();
        assertEquals("? extends java.lang.Number", result);
    }

    @Test
    public void testEmit_withObjectBound() throws IOException {
        StringBuilder output = new StringBuilder();
        CodeWriter codeWriter = new CodeWriter(output);
        WildcardTypeName wildcard = WildcardTypeName.subtypeOf(Object.class);

        wildcard.emit(codeWriter);

        String result = output.toString();
        assertEquals("?", result);
    }

    @Test
    public void testGet_withWildcardType() {
        WildcardTypeName wildcard1 = WildcardTypeName.subtypeOf(String.class);
        assertNotNull(wildcard1);
        assertEquals("? extends java.lang.String", wildcard1.toString());

        WildcardTypeName wildcard2 = WildcardTypeName.supertypeOf(Number.class);
        assertNotNull(wildcard2);
        assertEquals("? super java.lang.Number", wildcard2.toString());
    }
}
