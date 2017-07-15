package com.sun.corba.se.PortableActivationIDL.LocatorPackage;

public final class ServerLocationPerORBHolder implements org.omg.CORBA.portable.Streamable{
    public ServerLocationPerORB value=null;

    public ServerLocationPerORBHolder(){
    }

    public ServerLocationPerORBHolder(ServerLocationPerORB initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ServerLocationPerORBHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ServerLocationPerORBHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ServerLocationPerORBHelper.type();
    }
}
