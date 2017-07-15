/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public abstract class AnySeqHelper{
    private static String _id="IDL:omg.org/CORBA/AnySeq:1.0";
    private static TypeCode __typeCode=null;

    public static void insert(Any a,Any[] that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static TypeCode type(){
        if(__typeCode==null){
            __typeCode=ORB.init().get_primitive_tc(TCKind.tk_any);
            __typeCode=ORB.init().create_sequence_tc(0,__typeCode);
            __typeCode=ORB.init().create_alias_tc(AnySeqHelper.id(),"AnySeq",__typeCode);
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,Any[] value){
        ostream.write_long(value.length);
        for(int _i0=0;_i0<value.length;++_i0)
            ostream.write_any(value[_i0]);
    }

    public static Any[] extract(Any a){
        return read(a.create_input_stream());
    }

    public static Any[] read(org.omg.CORBA.portable.InputStream istream){
        Any value[]=null;
        int _len0=istream.read_long();
        value=new Any[_len0];
        for(int _o1=0;_o1<value.length;++_o1)
            value[_o1]=istream.read_any();
        return value;
    }
}
