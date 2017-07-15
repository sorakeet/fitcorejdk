package org.omg.CosNaming.NamingContextPackage;

public final class NotFoundHolder implements org.omg.CORBA.portable.Streamable{
    public NotFound value=null;

    public NotFoundHolder(){
    }

    public NotFoundHolder(NotFound initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=NotFoundHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        NotFoundHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return NotFoundHelper.type();
    }
}
