/**
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public interface Parameter{
    Type type();

    String name();

    String typeName();

    String toString();

    AnnotationDesc[] annotations();
}
