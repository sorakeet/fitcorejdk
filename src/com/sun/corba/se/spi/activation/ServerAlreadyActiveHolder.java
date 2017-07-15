package com.sun.corba.se.spi.activation;

public final class ServerAlreadyActiveHolder implements org.omg.CORBA.portable.Streamable{
    public ServerAlreadyActive value=null;

    public ServerAlreadyActiveHolder(){
    }

    public ServerAlreadyActiveHolder(ServerAlreadyActive initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ServerAlreadyActiveHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ServerAlreadyActiveHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ServerAlreadyActiveHelper.type();
    }
}
