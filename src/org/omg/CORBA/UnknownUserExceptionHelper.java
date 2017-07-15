/**
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

abstract public class UnknownUserExceptionHelper{
    private static String _id="IDL:omg.org/CORBA/UnknownUserException:1.0";
    private static TypeCode __typeCode=null;
    private static boolean __active=false;

    public static void insert(Any a,UnknownUserException that){
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
                    StructMember[] _members0=new StructMember[1];
                    TypeCode _tcOf_members0=null;
                    _tcOf_members0=ORB.init().get_primitive_tc(TCKind.tk_any);
                    _members0[0]=new StructMember(
                            "except",
                            _tcOf_members0,
                            null);
                    __typeCode=ORB.init().create_exception_tc(UnknownUserExceptionHelper.id(),"UnknownUserException",_members0);
                    __active=false;
                }
            }
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,UnknownUserException value){
        // write the repository ID
        ostream.write_string(id());
        ostream.write_any(value.except);
    }

    public static UnknownUserException extract(Any a){
        return read(a.create_input_stream());
    }

    public static UnknownUserException read(org.omg.CORBA.portable.InputStream istream){
        UnknownUserException value=new UnknownUserException();
        // read and discard the repository ID
        istream.read_string();
        value.except=istream.read_any();
        return value;
    }
}
