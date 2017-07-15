package com.sun.corba.se.spi.activation.RepositoryPackage;

public final class ServerDefHolder implements org.omg.CORBA.portable.Streamable{
    public ServerDef value=null;

    public ServerDefHolder(){
    }

    public ServerDefHolder(ServerDef initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ServerDefHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ServerDefHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ServerDefHelper.type();
    }
}
