package com.sun.corba.se.PortableActivationIDL;

public interface LocatorOperations{
    com.sun.corba.se.PortableActivationIDL.LocatorPackage.ServerLocationPerType locateServer(String serverId,String endPoint) throws NoSuchEndPoint, com.sun.corba.se.PortableActivationIDL.ServerNotRegistered, ServerHeldDown;

    com.sun.corba.se.PortableActivationIDL.LocatorPackage.ServerLocationPerORB locateServerForORB(String serverId,String orbId) throws InvalidORBid, com.sun.corba.se.PortableActivationIDL.ServerNotRegistered, ServerHeldDown;

    int getEndpoint(String endPointType) throws NoSuchEndPoint;

    int getServerPortForType(com.sun.corba.se.PortableActivationIDL.LocatorPackage.ServerLocationPerORB location,String endPointType) throws NoSuchEndPoint;
} // interface LocatorOperations
