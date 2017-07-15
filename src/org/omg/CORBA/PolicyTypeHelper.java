/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

// basic Policy definition
abstract public class PolicyTypeHelper{
    private static String _id="IDL:omg.org/CORBA/PolicyType:1.0";
    private static TypeCode __typeCode=null;

    public static void insert(Any a,int that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static TypeCode type(){
        if(__typeCode==null){
            __typeCode=ORB.init().get_primitive_tc(TCKind.tk_ulong);
            __typeCode=ORB.init().create_alias_tc(PolicyTypeHelper.id(),"PolicyType",__typeCode);
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,int value){
        ostream.write_ulong(value);
    }

    public static int extract(Any a){
        return read(a.create_input_stream());
    }

    public static int read(org.omg.CORBA.portable.InputStream istream){
        int value=(int)0;
        value=istream.read_ulong();
        return value;
    }
}
