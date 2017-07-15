package org.omg.PortableServer;

public interface ServantLocatorOperations extends ServantManagerOperations{
    Servant preinvoke(byte[] oid,POA adapter,String operation,org.omg.PortableServer.ServantLocatorPackage.CookieHolder the_cookie) throws ForwardRequest;

    void postinvoke(byte[] oid,POA adapter,String operation,Object the_cookie,Servant the_servant);
} // interface ServantLocatorOperations
