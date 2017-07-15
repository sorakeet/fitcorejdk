package com.sun.corba.se.spi.activation;

public final class BadServerDefinitionHolder implements org.omg.CORBA.portable.Streamable{
    public BadServerDefinition value=null;

    public BadServerDefinitionHolder(){
    }

    public BadServerDefinitionHolder(BadServerDefinition initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=BadServerDefinitionHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        BadServerDefinitionHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return BadServerDefinitionHelper.type();
    }
}
