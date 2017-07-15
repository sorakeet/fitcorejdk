package org.omg.CORBA;

public final class PolicyErrorHolder implements org.omg.CORBA.portable.Streamable{
    public PolicyError value=null;

    public PolicyErrorHolder(){
    }

    public PolicyErrorHolder(PolicyError initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=PolicyErrorHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        PolicyErrorHelper.write(o,value);
    }

    public TypeCode _type(){
        return PolicyErrorHelper.type();
    }
}
