package com.sun.corba.se.spi.activation;

public final class POANameHolder implements org.omg.CORBA.portable.Streamable{
    public String value[]=null;

    public POANameHolder(){
    }

    public POANameHolder(String[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=POANameHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        POANameHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return POANameHelper.type();
    }
}
