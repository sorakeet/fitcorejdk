/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.reflect;

public interface AnnotatedWildcardType extends AnnotatedType{
    AnnotatedType[] getAnnotatedLowerBounds();

    AnnotatedType[] getAnnotatedUpperBounds();
}
