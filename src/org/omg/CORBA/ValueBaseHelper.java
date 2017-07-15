/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * The Helper for <tt>ValueBase</tt>.  For more information on
 * Helper files, see <a href="doc-files/generatedfiles.html#helper">
 * "Generated Files: Helper Files"</a>.<P>
 * <p>
 * The Helper for <tt>ValueBase</tt>.  For more information on
 * Helper files, see <a href="doc-files/generatedfiles.html#helper">
 * "Generated Files: Helper Files"</a>.<P>
 * <p>
 * <p>
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 */
/**
 * The Helper for <tt>ValueBase</tt>.  For more information on
 * Helper files, see <a href="doc-files/generatedfiles.html#helper">
 * "Generated Files: Helper Files"</a>.<P>
 */
/**
 */
/**
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 */
package org.omg.CORBA;

abstract public class ValueBaseHelper{
    private static String _id="IDL:omg.org/CORBA/ValueBase:1.0";
    private static TypeCode __typeCode=null;

    public static void insert(Any a,java.io.Serializable that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static TypeCode type(){
        if(__typeCode==null){
            __typeCode=ORB.init().get_primitive_tc(TCKind.tk_value);
        }
        return __typeCode;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,java.io.Serializable value){
        ((org.omg.CORBA_2_3.portable.OutputStream)ostream).write_value(value);
    }

    public static java.io.Serializable extract(Any a){
        return read(a.create_input_stream());
    }

    public static java.io.Serializable read(org.omg.CORBA.portable.InputStream istream){
        return ((org.omg.CORBA_2_3.portable.InputStream)istream).read_value();
    }

    public static String id(){
        return _id;
    }
}
