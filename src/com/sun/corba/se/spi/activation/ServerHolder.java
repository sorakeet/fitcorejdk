package com.sun.corba.se.spi.activation;

public final class ServerHolder implements org.omg.CORBA.portable.Streamable{
    public Server value=null;

    public ServerHolder(){
    }

    public ServerHolder(Server initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ServerHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ServerHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ServerHelper.type();
    }
}
