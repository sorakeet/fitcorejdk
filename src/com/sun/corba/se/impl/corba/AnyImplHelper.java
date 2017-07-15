/**
 * Copyright (c) 1999, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 */
/**
 */
/**
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 */
package com.sun.corba.se.impl.corba;

abstract public class AnyImplHelper{
    private static String _id="IDL:omg.org/CORBA/Any:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,org.omg.CORBA.Any that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_any);
        }
        return __typeCode;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,org.omg.CORBA.Any value){
        ostream.write_any(value);
    }

    public static org.omg.CORBA.Any extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static org.omg.CORBA.Any read(org.omg.CORBA.portable.InputStream istream){
        return istream.read_any();
    }

    public static String id(){
        return _id;
    }
}
