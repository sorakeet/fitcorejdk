/**
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.PortableServer;

abstract public class POAHelper{
    private static String _id="IDL:omg.org/PortableServer/POA:2.3";
    private static org.omg.CORBA.TypeCode __typeCode=null;

    public static void insert(org.omg.CORBA.Any a,
                              POA that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static org.omg.CORBA.TypeCode type(){
        if(__typeCode==null){
            __typeCode=org.omg.CORBA.ORB.init().create_interface_tc(POAHelper.id(),"POA");
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,
                             POA value){
        throw new org.omg.CORBA.MARSHAL();
    }

    public static POA extract(org.omg.CORBA.Any a){
        return read(a.create_input_stream());
    }

    public static POA read(
            org.omg.CORBA.portable.InputStream istream){
        throw new org.omg.CORBA.MARSHAL();
    }

    public static POA narrow(org.omg.CORBA.Object obj){
        if(obj==null)
            return null;
        else if(obj instanceof POA)
            return (POA)obj;
        else if(!obj._is_a(id()))
            throw new org.omg.CORBA.BAD_PARAM();
        return null;
    }
}
