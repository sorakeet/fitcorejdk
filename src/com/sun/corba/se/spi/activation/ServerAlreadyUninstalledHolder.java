package com.sun.corba.se.spi.activation;

public final class ServerAlreadyUninstalledHolder implements org.omg.CORBA.portable.Streamable{
    public ServerAlreadyUninstalled value=null;

    public ServerAlreadyUninstalledHolder(){
    }

    public ServerAlreadyUninstalledHolder(ServerAlreadyUninstalled initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ServerAlreadyUninstalledHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ServerAlreadyUninstalledHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ServerAlreadyUninstalledHelper.type();
    }
}
