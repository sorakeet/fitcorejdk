package com.sun.corba.se.spi.activation;

public final class ServerAlreadyInstalledHolder implements org.omg.CORBA.portable.Streamable{
    public ServerAlreadyInstalled value=null;

    public ServerAlreadyInstalledHolder(){
    }

    public ServerAlreadyInstalledHolder(ServerAlreadyInstalled initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ServerAlreadyInstalledHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ServerAlreadyInstalledHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ServerAlreadyInstalledHelper.type();
    }
}
