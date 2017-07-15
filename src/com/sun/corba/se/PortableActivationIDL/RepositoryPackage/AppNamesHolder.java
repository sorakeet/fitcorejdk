package com.sun.corba.se.PortableActivationIDL.RepositoryPackage;

public final class AppNamesHolder implements org.omg.CORBA.portable.Streamable{
    public String value[]=null;

    public AppNamesHolder(){
    }

    public AppNamesHolder(String[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=AppNamesHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        AppNamesHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return AppNamesHelper.type();
    }
}
