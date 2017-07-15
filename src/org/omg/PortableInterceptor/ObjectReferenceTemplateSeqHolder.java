package org.omg.PortableInterceptor;

public final class ObjectReferenceTemplateSeqHolder implements org.omg.CORBA.portable.Streamable{
    public ObjectReferenceTemplate value[]=null;

    public ObjectReferenceTemplateSeqHolder(){
    }

    public ObjectReferenceTemplateSeqHolder(ObjectReferenceTemplate[] initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ObjectReferenceTemplateSeqHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ObjectReferenceTemplateSeqHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ObjectReferenceTemplateSeqHelper.type();
    }
}
