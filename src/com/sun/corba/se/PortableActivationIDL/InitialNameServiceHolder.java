package com.sun.corba.se.PortableActivationIDL;

public final class InitialNameServiceHolder implements org.omg.CORBA.portable.Streamable{
    public InitialNameService value=null;

    public InitialNameServiceHolder(){
    }

    public InitialNameServiceHolder(InitialNameService initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=InitialNameServiceHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        InitialNameServiceHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return InitialNameServiceHelper.type();
    }
}
