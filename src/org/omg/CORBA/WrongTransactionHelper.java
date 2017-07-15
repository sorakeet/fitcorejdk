/**
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

abstract public class WrongTransactionHelper{
    private static String _id="IDL:omg.org/CORBA/WrongTransaction:1.0";
    private static TypeCode __typeCode=null;
    private static boolean __active=false;

    public static void insert(Any a,WrongTransaction that){
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
                    StructMember[] _members0=new StructMember[0];
                    TypeCode _tcOf_members0=null;
                    __typeCode=ORB.init().create_exception_tc(WrongTransactionHelper.id(),"WrongTransaction",_members0);
                    __active=false;
                }
            }
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,WrongTransaction value){
        // write the repository ID
        ostream.write_string(id());
    }

    public static WrongTransaction extract(Any a){
        return read(a.create_input_stream());
    }

    public static WrongTransaction read(org.omg.CORBA.portable.InputStream istream){
        WrongTransaction value=new WrongTransaction();
        // read and discard the repository ID
        istream.read_string();
        return value;
    }
}
