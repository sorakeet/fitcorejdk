package org.omg.PortableInterceptor;

public final class ObjectReferenceTemplateHolder implements org.omg.CORBA.portable.Streamable{
    public ObjectReferenceTemplate value=null;

    public ObjectReferenceTemplateHolder(){
    }

    public ObjectReferenceTemplateHolder(ObjectReferenceTemplate initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ObjectReferenceTemplateHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ObjectReferenceTemplateHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ObjectReferenceTemplateHelper.type();
    }
}
