package com.sun.corba.se.PortableActivationIDL;

public final class ORBProxyHolder implements org.omg.CORBA.portable.Streamable{
    public ORBProxy value=null;

    public ORBProxyHolder(){
    }

    public ORBProxyHolder(ORBProxy initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ORBProxyHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ORBProxyHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ORBProxyHelper.type();
    }
}
