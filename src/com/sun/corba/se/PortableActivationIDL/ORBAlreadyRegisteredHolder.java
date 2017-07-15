package com.sun.corba.se.PortableActivationIDL;

public final class ORBAlreadyRegisteredHolder implements org.omg.CORBA.portable.Streamable{
    public ORBAlreadyRegistered value=null;

    public ORBAlreadyRegisteredHolder(){
    }

    public ORBAlreadyRegisteredHolder(ORBAlreadyRegistered initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ORBAlreadyRegisteredHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ORBAlreadyRegisteredHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ORBAlreadyRegisteredHelper.type();
    }
}
