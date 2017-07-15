package com.sun.corba.se.PortableActivationIDL;

public final class ServerProxyHolder implements org.omg.CORBA.portable.Streamable{
    public ServerProxy value=null;

    public ServerProxyHolder(){
    }

    public ServerProxyHolder(ServerProxy initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ServerProxyHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ServerProxyHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ServerProxyHelper.type();
    }
}
