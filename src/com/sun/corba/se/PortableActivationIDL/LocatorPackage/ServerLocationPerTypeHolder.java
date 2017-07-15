package com.sun.corba.se.PortableActivationIDL.LocatorPackage;

public final class ServerLocationPerTypeHolder implements org.omg.CORBA.portable.Streamable{
    public ServerLocationPerType value=null;

    public ServerLocationPerTypeHolder(){
    }

    public ServerLocationPerTypeHolder(ServerLocationPerType initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ServerLocationPerTypeHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ServerLocationPerTypeHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ServerLocationPerTypeHelper.type();
    }
}
