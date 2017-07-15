/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;

public interface NamedAndTyped{
    public String getName();

    public void setName(String name);

    public Type getType();

    public void setType(Type type);
}
