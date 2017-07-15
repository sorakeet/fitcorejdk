package org.omg.CosNaming;

public final class NamingContextHolder implements org.omg.CORBA.portable.Streamable{
    public NamingContext value=null;

    public NamingContextHolder(){
    }

    public NamingContextHolder(NamingContext initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=NamingContextHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        NamingContextHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return NamingContextHelper.type();
    }
}
