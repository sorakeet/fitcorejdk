/**
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.PortableServer;

abstract public class CurrentHelper{
    private static String _id="IDL:omg.org/PortableServer/Current:2.3";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,
                              Current that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=org.omg.CORBA.ORB.init().create_interface_tc(
                    CurrentHelper.id(),"Current");
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,
                             Current value){
        throw new org.omg.CORBA.MARSHAL();
    }

    public static Current extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static Current read(
            org.omg.CORBA.portable.InputStream istream){
        throw new org.omg.CORBA.MARSHAL();
    }

    public static Current narrow(
            org.omg.CORBA.Object obj){
        if(obj==null)
            return null;
        else if(obj instanceof Current)
            return (Current)obj;
        else if(!obj._is_a(id()))
            throw new org.omg.CORBA.BAD_PARAM();
        return null;
    }
}
