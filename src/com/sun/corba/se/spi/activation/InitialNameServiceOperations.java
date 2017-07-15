package com.sun.corba.se.spi.activation;

public interface InitialNameServiceOperations{
    // bind initial name
    void bind(String name,org.omg.CORBA.Object obj,boolean isPersistant) throws com.sun.corba.se.spi.activation.InitialNameServicePackage.NameAlreadyBound;
} // interface InitialNameServiceOperations
