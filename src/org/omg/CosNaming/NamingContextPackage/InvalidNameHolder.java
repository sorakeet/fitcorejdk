package org.omg.CosNaming.NamingContextPackage;

public final class InvalidNameHolder implements org.omg.CORBA.portable.Streamable{
    public InvalidName value=null;

    public InvalidNameHolder(){
    }

    public InvalidNameHolder(InvalidName initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=InvalidNameHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        InvalidNameHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return InvalidNameHelper.type();
    }
}
