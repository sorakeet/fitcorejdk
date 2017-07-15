package com.sun.corba.se.spi.activation;

public final class ServerAlreadyRegisteredHolder implements org.omg.CORBA.portable.Streamable{
    public ServerAlreadyRegistered value=null;

    public ServerAlreadyRegisteredHolder(){
    }

    public ServerAlreadyRegisteredHolder(ServerAlreadyRegistered initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ServerAlreadyRegisteredHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ServerAlreadyRegisteredHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ServerAlreadyRegisteredHelper.type();
    }
}
