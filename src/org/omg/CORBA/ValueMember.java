/**
 * Copyright (c) 1998, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * File: ./org/omg/CORBA/ValueMember.java
 * From: ./ir.idl
 * Date: Fri Aug 28 16:03:31 1998
 * By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */
/**
 * File: ./org/omg/CORBA/ValueMember.java
 * From: ./ir.idl
 * Date: Fri Aug 28 16:03:31 1998
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */
package org.omg.CORBA;

public final class ValueMember implements org.omg.CORBA.portable.IDLEntity{
    //  instance variables
    public String name;
    public String id;
    public String defined_in;
    public String version;
    public TypeCode type;
    public IDLType type_def;
    public short access;
    //  constructors

    public ValueMember(){
    }

    public ValueMember(String __name,String __id,String __defined_in,String __version,TypeCode __type,IDLType __type_def,short __access){
        name=__name;
        id=__id;
        defined_in=__defined_in;
        version=__version;
        type=__type;
        type_def=__type_def;
        access=__access;
    }
}
