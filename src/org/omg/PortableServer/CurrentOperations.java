package org.omg.PortableServer;

public interface CurrentOperations extends org.omg.CORBA.CurrentOperations{
    POA get_POA() throws org.omg.PortableServer.CurrentPackage.NoContext;

    byte[] get_object_id() throws org.omg.PortableServer.CurrentPackage.NoContext;
} // interface CurrentOperations
