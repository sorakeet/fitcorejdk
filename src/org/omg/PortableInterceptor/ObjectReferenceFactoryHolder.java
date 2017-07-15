package org.omg.PortableInterceptor;

public final class ObjectReferenceFactoryHolder implements org.omg.CORBA.portable.Streamable{
    public ObjectReferenceFactory value=null;

    public ObjectReferenceFactoryHolder(){
    }

    public ObjectReferenceFactoryHolder(ObjectReferenceFactory initialValue){
        value=initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i){
        value=ObjectReferenceFactoryHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o){
        ObjectReferenceFactoryHelper.write(o,value);
    }

    public org.omg.CORBA.TypeCode _type(){
        return ObjectReferenceFactoryHelper.type();
    }
}
