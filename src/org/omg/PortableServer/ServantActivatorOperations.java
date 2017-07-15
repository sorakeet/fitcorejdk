package org.omg.PortableServer;

public interface ServantActivatorOperations extends ServantManagerOperations{
    Servant incarnate(byte[] oid,POA adapter) throws ForwardRequest;

    void etherealize(byte[] oid,POA adapter,Servant serv,boolean cleanup_in_progress,boolean remaining_activations);
} // interface ServantActivatorOperations
