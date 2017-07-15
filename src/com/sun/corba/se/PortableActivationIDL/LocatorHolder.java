package com.sun.corba.se.PortableActivationIDL;

public final class LocatorHolder implements org.omg.CORBA.portable.Streamable{
    public Locator value=null;

    public LocatorHolder(){
    }

    public LocatorHolder(Locator initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=LocatorHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        LocatorHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return LocatorHelper.type();
    }
}
