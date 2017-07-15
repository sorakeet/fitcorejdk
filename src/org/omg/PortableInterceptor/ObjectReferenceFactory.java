package org.omg.PortableInterceptor;

public interface ObjectReferenceFactory extends org.omg.CORBA.portable.ValueBase{
    public abstract org.omg.CORBA.Object make_object(String repositoryId,byte[] object_id);
} // interface ObjectReferenceFactory
