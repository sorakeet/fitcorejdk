package org.omg.CosNaming.NamingContextPackage;

public final class CannotProceedHolder implements org.omg.CORBA.portable.Streamable{
    public CannotProceed value=null;

    public CannotProceedHolder(){
    }

    public CannotProceedHolder(CannotProceed initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=CannotProceedHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        CannotProceedHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return CannotProceedHelper.type();
    }
}
