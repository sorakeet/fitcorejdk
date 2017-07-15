/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

abstract public class NameValuePairHelper{
    private static String _id="IDL:omg.org/CORBA/NameValuePair:1.0";
    private static TypeCode __typeCode=null;
    private static boolean __active=false;

    public static void insert(Any a,NameValuePair that){
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
                    StructMember[] _members0=new StructMember[2];
                    TypeCode _tcOf_members0=null;
                    _tcOf_members0=ORB.init().create_string_tc(0);
                    _tcOf_members0=ORB.init().create_alias_tc(FieldNameHelper.id(),"FieldName",_tcOf_members0);
                    _members0[0]=new StructMember(
                            "id",
                            _tcOf_members0,
                            null);
                    _tcOf_members0=ORB.init().get_primitive_tc(TCKind.tk_any);
                    _members0[1]=new StructMember(
                            "value",
                            _tcOf_members0,
                            null);
                    __typeCode=ORB.init().create_struct_tc(NameValuePairHelper.id(),"NameValuePair",_members0);
                    __active=false;
                }
            }
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,NameValuePair value){
        ostream.write_string(value.id);
        ostream.write_any(value.value);
    }

    public static NameValuePair extract(Any a){
        return read(a.create_input_stream());
    }

    public static NameValuePair read(org.omg.CORBA.portable.InputStream istream){
        NameValuePair value=new NameValuePair();
        value.id=istream.read_string();
        value.value=istream.read_any();
        return value;
    }
}
