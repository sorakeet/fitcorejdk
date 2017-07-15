/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public class _IDLTypeStub extends org.omg.CORBA.portable.ObjectImpl implements IDLType{
    // Type-specific CORBA::Object operations
    private static String[] __ids={
            "IDL:omg.org/CORBA/IDLType:1.0",
            "IDL:omg.org/CORBA/IRObject:1.0"};

    // NOTE: This constructor is not required according to the spec. Only JCK expects it now.
    public _IDLTypeStub(){
        super();
    }

    // NOTE: This constructor is not required according to the spec. We keep it as a convenience method.
    public _IDLTypeStub(org.omg.CORBA.portable.Delegate delegate){
        super();
        _set_delegate(delegate);
    }

    public TypeCode type(){
        org.omg.CORBA.portable.InputStream _in=null;
        try{
            org.omg.CORBA.portable.OutputStream _out=_request("_get_type",true);
            _in=_invoke(_out);
            TypeCode __result=_in.read_TypeCode();
            return __result;
        }catch(org.omg.CORBA.portable.ApplicationException _ex){
            _in=_ex.getInputStream();
            String _id=_ex.getId();
            throw new MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException _rm){
            return type();
        }finally{
            _releaseReply(_in);
        }
    } // type

    // read interface
    public DefinitionKind def_kind(){
        org.omg.CORBA.portable.InputStream _in=null;
        try{
            org.omg.CORBA.portable.OutputStream _out=_request("_get_def_kind",true);
            _in=_invoke(_out);
            DefinitionKind __result=DefinitionKindHelper.read(_in);
            return __result;
        }catch(org.omg.CORBA.portable.ApplicationException _ex){
            _in=_ex.getInputStream();
            String _id=_ex.getId();
            throw new MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException _rm){
            return def_kind();
        }finally{
            _releaseReply(_in);
        }
    } // def_kind

    // write interface
    public void destroy(){
        org.omg.CORBA.portable.InputStream _in=null;
        try{
            org.omg.CORBA.portable.OutputStream _out=_request("destroy",true);
            _in=_invoke(_out);
        }catch(org.omg.CORBA.portable.ApplicationException _ex){
            _in=_ex.getInputStream();
            String _id=_ex.getId();
            throw new MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException _rm){
            destroy();
        }finally{
            _releaseReply(_in);
        }
    } // destroy

    public String[] _ids(){
        return (String[])__ids.clone();
    }

    private void readObject(java.io.ObjectInputStream s){
        try{
            String str=s.readUTF();
            Object obj=ORB.init().string_to_object(str);
            org.omg.CORBA.portable.Delegate delegate=((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
            _set_delegate(delegate);
        }catch(java.io.IOException e){
        }
    }

    private void writeObject(java.io.ObjectOutputStream s){
        try{
            String str=ORB.init().object_to_string(this);
            s.writeUTF(str);
        }catch(java.io.IOException e){
        }
    }
} // class _IDLTypeStub
