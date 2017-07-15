package org.omg.CosNaming;

public final class BindingTypeHolder implements org.omg.CORBA.portable.Streamable{
    public BindingType value=null;

    public BindingTypeHolder(){
    }

    public BindingTypeHolder(BindingType initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=BindingTypeHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        BindingTypeHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return BindingTypeHelper.type();
    }
}
