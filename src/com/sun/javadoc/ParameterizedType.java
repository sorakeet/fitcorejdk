/**
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public interface ParameterizedType extends Type{
    ClassDoc asClassDoc();

    Type[] typeArguments();

    Type superclassType();

    Type[] interfaceTypes();

    Type containingType();
}
