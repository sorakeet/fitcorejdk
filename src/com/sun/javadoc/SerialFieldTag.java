/**
 * Copyright (c) 1998, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public interface SerialFieldTag extends Tag, Comparable<Object>{
    public String fieldName();

    public String fieldType();

    public ClassDoc fieldTypeDoc();

    public String description();

    public int compareTo(Object obj);
}
