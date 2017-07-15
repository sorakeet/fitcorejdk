/**
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

@Deprecated
public interface DynEnum extends Object, DynAny{
    public String value_as_string();

    public void value_as_string(String arg);

    public int value_as_ulong();

    public void value_as_ulong(int arg);
}
