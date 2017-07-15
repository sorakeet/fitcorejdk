package org.omg.IOP;

public final class ServiceContextListHolder implements org.omg.CORBA.portable.Streamable{
    public ServiceContext value[]=null;

    public ServiceContextListHolder(){
    }

    public ServiceContextListHolder(ServiceContext[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ServiceContextListHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ServiceContextListHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ServiceContextListHelper.type();
    }
}
