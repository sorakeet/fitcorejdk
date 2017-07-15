package org.omg.PortableInterceptor;

public interface ObjectReferenceTemplate extends ObjectReferenceFactory{
    public abstract String server_id();

    public abstract String orb_id();

    public abstract String[] adapter_name();
} // interface ObjectReferenceTemplate
