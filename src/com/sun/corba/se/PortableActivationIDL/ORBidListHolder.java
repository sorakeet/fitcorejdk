package com.sun.corba.se.PortableActivationIDL;

public final class ORBidListHolder implements org.omg.CORBA.portable.Streamable{
    public String value[]=null;

    public ORBidListHolder(){
    }

    public ORBidListHolder(String[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ORBidListHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ORBidListHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ORBidListHelper.type();
    }
}
