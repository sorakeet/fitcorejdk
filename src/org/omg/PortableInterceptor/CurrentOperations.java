package org.omg.PortableInterceptor;

public interface CurrentOperations extends org.omg.CORBA.CurrentOperations{
    org.omg.CORBA.Any get_slot(int id) throws InvalidSlot;

    void set_slot(int id,org.omg.CORBA.Any data) throws InvalidSlot;
} // interface CurrentOperations
