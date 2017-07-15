package org.omg.IOP;

public final class ServiceContextHolder implements org.omg.CORBA.portable.Streamable{
    public ServiceContext value=null;

    public ServiceContextHolder(){
    }

    public ServiceContextHolder(ServiceContext initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ServiceContextHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ServiceContextHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ServiceContextHelper.type();
    }
}
