/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public abstract class DoubleSeqHelper{
    private static String _id="IDL:omg.org/CORBA/DoubleSeq:1.0";
    private static TypeCode __typeCode=null;

    public static void insert(Any a,double[] that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static TypeCode type(){
        if(__typeCode==null){
            __typeCode=ORB.init().get_primitive_tc(TCKind.tk_double);
            __typeCode=ORB.init().create_sequence_tc(0,__typeCode);
            __typeCode=ORB.init().create_alias_tc(DoubleSeqHelper.id(),"DoubleSeq",__typeCode);
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,double[] value){
        ostream.write_long(value.length);
        ostream.write_double_array(value,0,value.length);
    }

    public static double[] extract(Any a){
        return read(a.create_input_stream());
    }

    public static double[] read(org.omg.CORBA.portable.InputStream istream){
        double value[]=null;
        int _len0=istream.read_long();
        value=new double[_len0];
        istream.read_double_array(value,0,_len0);
        return value;
    }
}
