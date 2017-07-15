/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.protocol.giopmsgheaders;

abstract public class IORAddressingInfoHelper{
    private static String _id="IDL:messages/IORAddressingInfo:1.0";
    private static org.omg.CORBA.TypeCode __typeCode=null;
    private static boolean __active=false;

    public static void insert(org.omg.CORBA.Any a,IORAddressingInfo that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            synchronized(org.omg.CORBA.TypeCode.class){
                if(__typeCode==null){
                    if(__active){
                        return org.omg.CORBA.ORB.init().create_recursive_tc(_id);
                    }
                    __active=true;
                    org.omg.CORBA.StructMember[] _members0=new org.omg.CORBA.StructMember[2];
                    org.omg.CORBA.TypeCode _tcOf_members0=null;
                    _tcOf_members0=org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_ulong);
                    _members0[0]=new org.omg.CORBA.StructMember(
                            "selected_profile_index",
                            _tcOf_members0,
                            null);
                    _tcOf_members0=org.omg.IOP.IORHelper.type();
                    _members0[1]=new org.omg.CORBA.StructMember(
                            "ior",
                            _tcOf_members0,
                            null);
                    __typeCode=org.omg.CORBA.ORB.init().create_struct_tc(IORAddressingInfoHelper.id(),"IORAddressingInfo",_members0);
                    __active=false;
                }
            }
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,IORAddressingInfo value){
        ostream.write_ulong(value.selected_profile_index);
        org.omg.IOP.IORHelper.write(ostream,value.ior);
    }

    public static IORAddressingInfo extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static IORAddressingInfo read(org.omg.CORBA.portable.InputStream istream){
        IORAddressingInfo value=new IORAddressingInfo();
        value.selected_profile_index=istream.read_ulong();
        value.ior=org.omg.IOP.IORHelper.read(istream);
        return value;
    }
}
