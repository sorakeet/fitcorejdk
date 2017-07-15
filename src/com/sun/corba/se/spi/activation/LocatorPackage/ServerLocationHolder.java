package com.sun.corba.se.spi.activation.LocatorPackage;

public final class ServerLocationHolder implements org.omg.CORBA.portable.Streamable{
    public ServerLocation value=null;

    public ServerLocationHolder(){
    }

    public ServerLocationHolder(ServerLocation initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ServerLocationHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ServerLocationHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ServerLocationHelper.type();
    }
}
