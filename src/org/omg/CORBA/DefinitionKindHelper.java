/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

abstract public class DefinitionKindHelper{
    private static String _id="IDL:omg.org/CORBA/DefinitionKind:1.0";
    private static TypeCode __typeCode=null;

    public static void insert(Any a,DefinitionKind that){
        org.omg.CORBA.portable.OutputStream out=a.create_output_stream();
        a.type(type());
        write(out,that);
        a.read_value(out.create_input_stream(),type());
    }

    synchronized public static TypeCode type(){
        if(__typeCode==null){
            __typeCode=ORB.init().create_enum_tc(DefinitionKindHelper.id(),"DefinitionKind",new String[]{"dk_none","dk_all","dk_Attribute","dk_Constant","dk_Exception","dk_Interface","dk_Module","dk_Operation","dk_Typedef","dk_Alias","dk_Struct","dk_Union","dk_Enum","dk_Primitive","dk_String","dk_Sequence","dk_Array","dk_Repository","dk_Wstring","dk_Fixed","dk_Value","dk_ValueBox","dk_ValueMember","dk_Native"});
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,DefinitionKind value){
        ostream.write_long(value.value());
    }

    public static DefinitionKind extract(Any a){
        return read(a.create_input_stream());
    }

    public static DefinitionKind read(org.omg.CORBA.portable.InputStream istream){
        return DefinitionKind.from_int(istream.read_long());
    }
}
