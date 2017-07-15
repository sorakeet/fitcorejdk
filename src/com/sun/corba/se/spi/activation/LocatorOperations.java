package com.sun.corba.se.spi.activation;

public interface LocatorOperations{
    // Starts the server if it is not already running.
    com.sun.corba.se.spi.activation.LocatorPackage.ServerLocation locateServer(int serverId,String endPoint) throws NoSuchEndPoint, ServerNotRegistered, ServerHeldDown;

    // Starts the server if it is not already running.
    com.sun.corba.se.spi.activation.LocatorPackage.ServerLocationPerORB locateServerForORB(int serverId,String orbId) throws InvalidORBid, ServerNotRegistered, ServerHeldDown;

    // get the port for the endpoint of the locator
    int getEndpoint(String endPointType) throws NoSuchEndPoint;

    // to pick a particular port type.
    int getServerPortForType(com.sun.corba.se.spi.activation.LocatorPackage.ServerLocationPerORB location,String endPointType) throws NoSuchEndPoint;
} // interface LocatorOperations
