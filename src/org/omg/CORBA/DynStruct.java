/**
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

@Deprecated
public interface DynStruct extends Object, DynAny{
    public String current_member_name();

    public TCKind current_member_kind();

    public NameValuePair[] get_members();

    public void set_members(NameValuePair[] value)
            throws org.omg.CORBA.DynAnyPackage.InvalidSeq;
}
