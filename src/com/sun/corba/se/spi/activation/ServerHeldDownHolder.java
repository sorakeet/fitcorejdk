package com.sun.corba.se.spi.activation;

public final class ServerHeldDownHolder implements org.omg.CORBA.portable.Streamable{
    public ServerHeldDown value=null;

    public ServerHeldDownHolder(){
    }

    public ServerHeldDownHolder(ServerHeldDown initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ServerHeldDownHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ServerHeldDownHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ServerHeldDownHelper.type();
    }
}
