/**
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

@Deprecated
public interface DynSequence extends Object, DynAny{
    public int length();

    public void length(int arg);

    public Any[] get_elements();

    public void set_elements(Any[] value)
            throws org.omg.CORBA.DynAnyPackage.InvalidSeq;
}
