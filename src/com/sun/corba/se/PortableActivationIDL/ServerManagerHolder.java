package com.sun.corba.se.PortableActivationIDL;

public final class ServerManagerHolder implements org.omg.CORBA.portable.Streamable{
    public ServerManager value=null;

    public ServerManagerHolder(){
    }

    public ServerManagerHolder(ServerManager initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ServerManagerHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ServerManagerHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ServerManagerHelper.type();
    }
}
