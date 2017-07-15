/**
 * Copyright (c) 1997, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * File: ./org/omg/CORBA/StructMember.java
 * From: ./ir.idl
 * Date: Fri Aug 28 16:03:31 1998
 * By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */
/**
 * File: ./org/omg/CORBA/StructMember.java
 * From: ./ir.idl
 * Date: Fri Aug 28 16:03:31 1998
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */
package org.omg.CORBA;

public final class StructMember implements org.omg.CORBA.portable.IDLEntity{
    //  instance variables
    public String name;
    public TypeCode type;
    public IDLType type_def;
    //  constructors

    public StructMember(){
    }

    public StructMember(String __name,TypeCode __type,IDLType __type_def){
        name=__name;
        type=__type;
        type_def=__type_def;
    }
}
