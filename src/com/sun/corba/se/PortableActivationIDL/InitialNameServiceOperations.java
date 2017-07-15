package com.sun.corba.se.PortableActivationIDL;

public interface InitialNameServiceOperations{
    void bind(String name,org.omg.CORBA.Object obj,boolean isPersistant) throws com.sun.corba.se.PortableActivationIDL.InitialNameServicePackage.NameAlreadyBound;
} // interface InitialNameServiceOperations
