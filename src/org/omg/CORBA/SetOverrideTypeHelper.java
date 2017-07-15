/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

abstract public class SetOverrideTypeHelper{
    private static String _id="IDL:omg.org/CORBA/SetOverrideType:1.0";
    private static TypeCode __typeCode=null;

    public static void insert(Any a,SetOverrideType that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static TypeCode type(){
        if(__typeCode==null){
            __typeCode=ORB.init().create_enum_tc(SetOverrideTypeHelper.id(),"SetOverrideType",new String[]{"SET_OVERRIDE","ADD_OVERRIDE"});
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,SetOverrideType value){
        ostream.write_long(value.value());
    }

    public static SetOverrideType extract(Any a){
        return read(a.create_input_stream());
    }

    public static SetOverrideType read(org.omg.CORBA.portable.InputStream istream){
        return SetOverrideType.from_int(istream.read_long());
    }
}
