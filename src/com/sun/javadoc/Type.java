/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public interface Type{
    String typeName();

    String qualifiedTypeName();

    String simpleTypeName();

    String dimension();

    String toString();

    boolean isPrimitive();

    ClassDoc asClassDoc();

    ParameterizedType asParameterizedType();

    TypeVariable asTypeVariable();

    WildcardType asWildcardType();

    AnnotatedType asAnnotatedType();

    AnnotationTypeDoc asAnnotationTypeDoc();

    Type getElementType();
}
