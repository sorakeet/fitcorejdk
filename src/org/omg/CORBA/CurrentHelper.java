/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

abstract public class CurrentHelper{
    private static String _id="IDL:omg.org/CORBA/Current:1.0";
    private static TypeCode __typeCode=null;

    public static void insert(Any a,Current that){
        throw new MARSHAL();
    }

    public static Current extract(Any a){
        throw new MARSHAL();
    }

    synchronized public static TypeCode type(){
        if(__typeCode==null){
            __typeCode=ORB.init().create_interface_tc(CurrentHelper.id(),"Current");
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static Current read(org.omg.CORBA.portable.InputStream istream){
        throw new MARSHAL();
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,Current value){
        throw new MARSHAL();
    }

    public static Current narrow(Object obj){
        if(obj==null)
            return null;
        else if(obj instanceof Current)
            return (Current)obj;
        else
            throw new BAD_PARAM();
    }
}
