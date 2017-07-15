package org.omg.CosNaming;

public final class NameComponentHolder implements org.omg.CORBA.portable.Streamable{
    public NameComponent value=null;

    public NameComponentHolder(){
    }

    public NameComponentHolder(NameComponent initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=NameComponentHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        NameComponentHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return NameComponentHelper.type();
    }
}
