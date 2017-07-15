/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.annotation;

public interface Annotation{
    int hashCode();

    boolean equals(Object obj);

    String toString();

    Class<? extends Annotation> annotationType();
}
