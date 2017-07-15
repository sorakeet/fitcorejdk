/**
 * Copyright (c) 1996, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public abstract class ExceptionList{
    public abstract int count();

    public abstract void add(TypeCode exc);

    public abstract TypeCode item(int index)
            throws Bounds;

    public abstract void remove(int index)
            throws Bounds;
}
