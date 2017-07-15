package com.sun.corba.se.spi.activation;

public final class ServerNotRegisteredHolder implements org.omg.CORBA.portable.Streamable{
    public ServerNotRegistered value=null;

    public ServerNotRegisteredHolder(){
    }

    public ServerNotRegisteredHolder(ServerNotRegistered initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ServerNotRegisteredHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ServerNotRegisteredHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ServerNotRegisteredHelper.type();
    }
}
