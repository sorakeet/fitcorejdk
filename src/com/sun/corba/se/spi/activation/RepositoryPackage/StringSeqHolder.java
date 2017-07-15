package com.sun.corba.se.spi.activation.RepositoryPackage;

public final class StringSeqHolder implements org.omg.CORBA.portable.Streamable{
    public String value[]=null;

    public StringSeqHolder(){
    }

    public StringSeqHolder(String[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=StringSeqHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        StringSeqHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return StringSeqHelper.type();
    }
}
