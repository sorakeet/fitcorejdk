/**
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

@Deprecated
public interface DynFixed extends Object, DynAny{
    public byte[] get_value();

    public void set_value(byte[] val)
            throws org.omg.CORBA.DynAnyPackage.InvalidValue;
}
