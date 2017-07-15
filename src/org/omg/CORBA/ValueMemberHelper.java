/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

abstract public class ValueMemberHelper{
    private static String _id="IDL:omg.org/CORBA/ValueMember:1.0";
    private static TypeCode __typeCode=null;
    private static boolean __active=false;

    public static void insert(Any a,ValueMember that){
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
                    StructMember[] _members0=new StructMember[7];
                    TypeCode _tcOf_members0=null;
                    _tcOf_members0=ORB.init().create_string_tc(0);
                    _tcOf_members0=ORB.init().create_alias_tc(IdentifierHelper.id(),"Identifier",_tcOf_members0);
                    _members0[0]=new StructMember(
                            "name",
                            _tcOf_members0,
                            null);
                    _tcOf_members0=ORB.init().create_string_tc(0);
                    _tcOf_members0=ORB.init().create_alias_tc(RepositoryIdHelper.id(),"RepositoryId",_tcOf_members0);
                    _members0[1]=new StructMember(
                            "id",
                            _tcOf_members0,
                            null);
                    _tcOf_members0=ORB.init().create_string_tc(0);
                    _tcOf_members0=ORB.init().create_alias_tc(RepositoryIdHelper.id(),"RepositoryId",_tcOf_members0);
                    _members0[2]=new StructMember(
                            "defined_in",
                            _tcOf_members0,
                            null);
                    _tcOf_members0=ORB.init().create_string_tc(0);
                    _tcOf_members0=ORB.init().create_alias_tc(VersionSpecHelper.id(),"VersionSpec",_tcOf_members0);
                    _members0[3]=new StructMember(
                            "version",
                            _tcOf_members0,
                            null);
                    _tcOf_members0=ORB.init().get_primitive_tc(TCKind.tk_TypeCode);
                    _members0[4]=new StructMember(
                            "type",
                            _tcOf_members0,
                            null);
                    _tcOf_members0=IDLTypeHelper.type();
                    _members0[5]=new StructMember(
                            "type_def",
                            _tcOf_members0,
                            null);
                    _tcOf_members0=ORB.init().get_primitive_tc(TCKind.tk_short);
                    _tcOf_members0=ORB.init().create_alias_tc(VisibilityHelper.id(),"Visibility",_tcOf_members0);
                    _members0[6]=new StructMember(
                            "access",
                            _tcOf_members0,
                            null);
                    __typeCode=ORB.init().create_struct_tc(ValueMemberHelper.id(),"ValueMember",_members0);
                    __active=false;
                }
            }
        }
        return __typeCode;
    }

    public static String id(){
        return _id;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,ValueMember value){
        ostream.write_string(value.name);
        ostream.write_string(value.id);
        ostream.write_string(value.defined_in);
        ostream.write_string(value.version);
        ostream.write_TypeCode(value.type);
        IDLTypeHelper.write(ostream,value.type_def);
        ostream.write_short(value.access);
    }

    public static ValueMember extract(Any a){
        return read(a.create_input_stream());
    }

    public static ValueMember read(org.omg.CORBA.portable.InputStream istream){
        ValueMember value=new ValueMember();
        value.name=istream.read_string();
        value.id=istream.read_string();
        value.defined_in=istream.read_string();
        value.version=istream.read_string();
        value.type=istream.read_TypeCode();
        value.type_def=IDLTypeHelper.read(istream);
        value.access=istream.read_short();
        return value;
    }
}
