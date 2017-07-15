package com.sun.corba.se.spi.activation;

public final class NoSuchEndPointHolder implements org.omg.CORBA.portable.Streamable{
    public NoSuchEndPoint value=null;

    public NoSuchEndPointHolder(){
    }

    public NoSuchEndPointHolder(NoSuchEndPoint initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=NoSuchEndPointHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        NoSuchEndPointHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return NoSuchEndPointHelper.type();
    }
}
