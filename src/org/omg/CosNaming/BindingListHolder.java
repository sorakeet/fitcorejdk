package org.omg.CosNaming;

public final class BindingListHolder implements org.omg.CORBA.portable.Streamable{
    public Binding value[]=null;

    public BindingListHolder(){
    }

    public BindingListHolder(Binding[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=BindingListHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        BindingListHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return BindingListHelper.type();
    }
}
