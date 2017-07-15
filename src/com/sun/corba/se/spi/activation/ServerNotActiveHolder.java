package com.sun.corba.se.spi.activation;

public final class ServerNotActiveHolder implements org.omg.CORBA.portable.Streamable{
    public ServerNotActive value=null;

    public ServerNotActiveHolder(){
    }

    public ServerNotActiveHolder(ServerNotActive initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ServerNotActiveHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ServerNotActiveHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ServerNotActiveHelper.type();
    }
}
