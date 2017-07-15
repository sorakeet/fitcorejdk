/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public interface WildcardType extends Type{
    Type[] extendsBounds();

    Type[] superBounds();
}
