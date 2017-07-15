package com.sun.corba.se.spi.activation;

public final class RepositoryHolder implements org.omg.CORBA.portable.Streamable{
    public Repository value=null;

    public RepositoryHolder(){
    }

    public RepositoryHolder(Repository initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=RepositoryHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        RepositoryHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return RepositoryHelper.type();
    }
}
