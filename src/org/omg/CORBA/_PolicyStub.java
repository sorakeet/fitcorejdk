/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public class _PolicyStub extends org.omg.CORBA.portable.ObjectImpl implements Policy{
    // Type-specific CORBA::Object operations
    private static String[] __ids={
            "IDL:omg.org/CORBA/Policy:1.0"};

    public _PolicyStub(){
        super();
    }

    public _PolicyStub(org.omg.CORBA.portable.Delegate delegate){
        super();
        _set_delegate(delegate);
    }

    public int policy_type(){
        org.omg.CORBA.portable.InputStream _in=null;
        try{
            org.omg.CORBA.portable.OutputStream _out=_request("_get_policy_type",true);
            _in=_invoke(_out);
            int __result=PolicyTypeHelper.read(_in);
            return __result;
        }catch(org.omg.CORBA.portable.ApplicationException _ex){
            _in=_ex.getInputStream();
            String _id=_ex.getId();
            throw new MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException _rm){
            return policy_type();
        }finally{
            _releaseReply(_in);
        }
    } // policy_type

    public Policy copy(){
        org.omg.CORBA.portable.InputStream _in=null;
        try{
            org.omg.CORBA.portable.OutputStream _out=_request("copy",true);
            _in=_invoke(_out);
            Policy __result=PolicyHelper.read(_in);
            return __result;
        }catch(org.omg.CORBA.portable.ApplicationException _ex){
            _in=_ex.getInputStream();
            String _id=_ex.getId();
            throw new MARSHAL(_id);
        }catch(org.omg.CORBA.portable.RemarshalException _rm){
            return copy();
        }finally{
            _releaseReply(_in);
        }
    } // copy

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
} // class _PolicyStub
