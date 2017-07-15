/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

abstract public class VersionSpecHelper{
    private static String _id="IDL:omg.org/CORBA/VersionSpec:1.0";
    private static TypeCode __typeCode=null;

    public static void insert(Any a,String that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static TypeCode type(){
        if(__typeCode==null){
            __typeCode=ORB.init().create_string_tc(0);
            __typeCode=ORB.init().create_alias_tc(VersionSpecHelper.id(),"VersionSpec",__typeCode);
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,String value){
        ostream.write_string(value);
    }

    public static String extract(Any a){
        return read(a.create_input_stream());
    }

    public static String read(org.omg.CORBA.portable.InputStream istream){
        String value=null;
        value=istream.read_string();
        return value;
    }
}
