package org.omg.CosNaming.NamingContextPackage;

public final class NotEmptyHolder implements org.omg.CORBA.portable.Streamable{
    public NotEmpty value=null;

    public NotEmptyHolder(){
    }

    public NotEmptyHolder(NotEmpty initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=NotEmptyHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        NotEmptyHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return NotEmptyHelper.type();
    }
}
