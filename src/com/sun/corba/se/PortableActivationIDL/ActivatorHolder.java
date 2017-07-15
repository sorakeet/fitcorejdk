package com.sun.corba.se.PortableActivationIDL;

public final class ActivatorHolder implements org.omg.CORBA.portable.Streamable{
    public Activator value=null;

    public ActivatorHolder(){
    }

    public ActivatorHolder(Activator initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ActivatorHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ActivatorHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ActivatorHelper.type();
    }
}
