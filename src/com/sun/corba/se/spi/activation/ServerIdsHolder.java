package com.sun.corba.se.spi.activation;

public final class ServerIdsHolder implements org.omg.CORBA.portable.Streamable{
    public int value[]=null;

    public ServerIdsHolder(){
    }

    public ServerIdsHolder(int[] initialValue){
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
