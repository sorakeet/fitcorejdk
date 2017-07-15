package org.omg.CosNaming;

public final class BindingIteratorHolder implements org.omg.CORBA.portable.Streamable{
    public BindingIterator value=null;

    public BindingIteratorHolder(){
    }

    public BindingIteratorHolder(BindingIterator initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=BindingIteratorHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        BindingIteratorHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return BindingIteratorHelper.type();
    }
}
