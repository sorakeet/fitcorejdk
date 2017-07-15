package com.sun.corba.se.spi.activation;

public final class EndpointInfoListHolder implements org.omg.CORBA.portable.Streamable{
    public EndPointInfo value[]=null;

    public EndpointInfoListHolder(){
    }

    public EndpointInfoListHolder(EndPointInfo[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=EndpointInfoListHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        EndpointInfoListHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return EndpointInfoListHelper.type();
    }
}
