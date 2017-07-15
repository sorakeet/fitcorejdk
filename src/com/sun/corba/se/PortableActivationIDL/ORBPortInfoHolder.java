package com.sun.corba.se.PortableActivationIDL;

public final class ORBPortInfoHolder implements org.omg.CORBA.portable.Streamable{
    public ORBPortInfo value=null;

    public ORBPortInfoHolder(){
    }

    public ORBPortInfoHolder(ORBPortInfo initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ORBPortInfoHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ORBPortInfoHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ORBPortInfoHelper.type();
    }
}
