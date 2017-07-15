package com.sun.corba.se.spi.activation;

public final class ORBPortInfoListHolder implements org.omg.CORBA.portable.Streamable{
    public ORBPortInfo value[]=null;

    public ORBPortInfoListHolder(){
    }

    public ORBPortInfoListHolder(ORBPortInfo[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ORBPortInfoListHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ORBPortInfoListHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ORBPortInfoListHelper.type();
    }
}
