package org.omg.CosNaming.NamingContextPackage;

public final class NotFoundReasonHolder implements org.omg.CORBA.portable.Streamable{
    public NotFoundReason value=null;

    public NotFoundReasonHolder(){
    }

    public NotFoundReasonHolder(NotFoundReason initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=NotFoundReasonHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        NotFoundReasonHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return NotFoundReasonHelper.type();
    }
}
