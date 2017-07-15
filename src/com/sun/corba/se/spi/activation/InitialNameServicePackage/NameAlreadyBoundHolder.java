package com.sun.corba.se.spi.activation.InitialNameServicePackage;

public final class NameAlreadyBoundHolder implements org.omg.CORBA.portable.Streamable{
    public NameAlreadyBound value=null;

    public NameAlreadyBoundHolder(){
    }

    public NameAlreadyBoundHolder(NameAlreadyBound initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=NameAlreadyBoundHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        NameAlreadyBoundHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return NameAlreadyBoundHelper.type();
    }
}
