/**
 * Copyright (c) 1998, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 */
/**
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 */
package org.omg.CORBA;

public class StringValueHelper implements org.omg.CORBA.portable.BoxedValueHelper{
    private static String _id="IDL:omg.org/CORBA/StringValue:1.0";
    private static StringValueHelper _instance=new StringValueHelper();
    private static TypeCode __typeCode=null;
    private static boolean __active=false;

    public static void insert(Any a,String that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static TypeCode type(){
        if(__typeCode==null){
            synchronized(TypeCode.class){
                if(__typeCode==null){
                    if(__active){
                        return ORB.init().create_recursive_tc(_id);
                    }
                    __active=true;
                    __typeCode=ORB.init().create_string_tc(0);
                    __typeCode=ORB.init().create_value_box_tc(_id,"StringValue",__typeCode);
                    __active=false;
                }
            }
        }
        return __typeCode;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,String value){
        if(!(ostream instanceof org.omg.CORBA_2_3.portable.OutputStream)){
            throw new BAD_PARAM();
        }
        ((org.omg.CORBA_2_3.portable.OutputStream)ostream).write_value(value,_instance);
    }

    public static String extract(Any a){
        return read(a.create_input_stream());
    }

    public static String read(org.omg.CORBA.portable.InputStream istream){
        if(!(istream instanceof org.omg.CORBA_2_3.portable.InputStream)){
            throw new BAD_PARAM();
        }
        return (String)((org.omg.CORBA_2_3.portable.InputStream)istream).read_value(_instance);
    }

    public static String id(){
        return _id;
    }

    public java.io.Serializable read_value(org.omg.CORBA.portable.InputStream istream){
        String tmp;
        tmp=istream.read_string();
        return (java.io.Serializable)tmp;
    }

    public void write_value(org.omg.CORBA.portable.OutputStream ostream,java.io.Serializable value){
        if(!(value instanceof String)){
            throw new MARSHAL();
        }
        String valueType=(String)value;
        ostream.write_string(valueType);
    }

    public String get_id(){
        return _id;
    }
}
