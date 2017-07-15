package com.sun.corba.se.PortableActivationIDL;

public final class EndPointInfoHolder implements org.omg.CORBA.portable.Streamable{
    public EndPointInfo value=null;

    public EndPointInfoHolder(){
    }

    public EndPointInfoHolder(EndPointInfo initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=EndPointInfoHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        EndPointInfoHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return EndPointInfoHelper.type();
    }
}
