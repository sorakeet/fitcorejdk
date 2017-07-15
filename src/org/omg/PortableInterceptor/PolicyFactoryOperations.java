package org.omg.PortableInterceptor;

public interface PolicyFactoryOperations{
    org.omg.CORBA.Policy create_policy(int type,org.omg.CORBA.Any value) throws org.omg.CORBA.PolicyError;
} // interface PolicyFactoryOperations
