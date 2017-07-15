/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

abstract public class PolicyListHelper{
    private static String _id="IDL:omg.org/CORBA/PolicyList:1.0";
    private static TypeCode __typeCode=null;

    public static void insert(Any a,Policy[] that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static TypeCode type(){
        if(__typeCode==null){
            __typeCode=PolicyHelper.type();
            __typeCode=ORB.init().create_sequence_tc(0,__typeCode);
            __typeCode=ORB.init().create_alias_tc(PolicyListHelper.id(),"PolicyList",__typeCode);
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,Policy[] value){
        ostream.write_long(value.length);
        for(int _i0=0;_i0<value.length;++_i0)
            PolicyHelper.write(ostream,value[_i0]);
    }

    public static Policy[] extract(Any a){
        return read(a.create_input_stream());
    }

    public static Policy[] read(org.omg.CORBA.portable.InputStream istream){
        Policy value[]=null;
        int _len0=istream.read_long();
        value=new Policy[_len0];
        for(int _o1=0;_o1<value.length;++_o1)
            value[_o1]=PolicyHelper.read(istream);
        return value;
    }
}
