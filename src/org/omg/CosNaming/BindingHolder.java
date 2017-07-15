package org.omg.CosNaming;

public final class BindingHolder implements org.omg.CORBA.portable.Streamable{
    public Binding value=null;

    public BindingHolder(){
    }

    public BindingHolder(Binding initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=BindingHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        BindingHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return BindingHelper.type();
    }
}
