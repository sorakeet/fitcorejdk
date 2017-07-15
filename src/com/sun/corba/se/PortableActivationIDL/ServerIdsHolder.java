package com.sun.corba.se.PortableActivationIDL;

public final class ServerIdsHolder implements org.omg.CORBA.portable.Streamable{
    public String value[]=null;

    public ServerIdsHolder(){
    }

    public ServerIdsHolder(String[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ServerIdsHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ServerIdsHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ServerIdsHelper.type();
    }
}
