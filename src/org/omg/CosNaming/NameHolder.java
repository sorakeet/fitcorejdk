package org.omg.CosNaming;

public final class NameHolder implements org.omg.CORBA.portable.Streamable{
    public NameComponent value[]=null;

    public NameHolder(){
    }

    public NameHolder(NameComponent[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=NameHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        NameHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return NameHelper.type();
    }
}
