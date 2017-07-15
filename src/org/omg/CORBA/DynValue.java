/**
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

@Deprecated
public interface DynValue extends Object, DynAny{
    String current_member_name();

    TCKind current_member_kind();

    NameValuePair[] get_members();

    void set_members(NameValuePair[] value)
            throws org.omg.CORBA.DynAnyPackage.InvalidSeq;
}
