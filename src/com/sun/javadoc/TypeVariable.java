/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public interface TypeVariable extends Type{
    Type[] bounds();

    ProgramElementDoc owner();

    public AnnotationDesc[] annotations();
}
