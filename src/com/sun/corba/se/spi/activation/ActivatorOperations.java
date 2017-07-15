package com.sun.corba.se.spi.activation;

public interface ActivatorOperations{
    // A new ORB started server registers itself with the Activator
    void active(int serverId,Server serverObj) throws ServerNotRegistered;

    // Install a particular kind of endpoint
    void registerEndpoints(int serverId,String orbId,EndPointInfo[] endPointInfo) throws ServerNotRegistered, NoSuchEndPoint, ORBAlreadyRegistered;

    // list active servers
    int[] getActiveServers();

    // If the server is not running, start it up.
    void activate(int serverId) throws ServerAlreadyActive, ServerNotRegistered, ServerHeldDown;

    // If the server is running, shut it down
    void shutdown(int serverId) throws ServerNotActive, ServerNotRegistered;

    // currently running, this method will activate it.
    void install(int serverId) throws ServerNotRegistered, ServerHeldDown, ServerAlreadyInstalled;

    // list all registered ORBs for a server
    String[] getORBNames(int serverId) throws ServerNotRegistered;

    // After this hook completes, the server may still be running.
    void uninstall(int serverId) throws ServerNotRegistered, ServerHeldDown, ServerAlreadyUninstalled;
} // interface ActivatorOperations
