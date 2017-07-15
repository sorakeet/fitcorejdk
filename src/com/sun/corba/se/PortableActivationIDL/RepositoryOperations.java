package com.sun.corba.se.PortableActivationIDL;

public interface RepositoryOperations{
    String registerServer(com.sun.corba.se.PortableActivationIDL.RepositoryPackage.ServerDef serverDef) throws com.sun.corba.se.PortableActivationIDL.ServerAlreadyRegistered, BadServerDefinition;

    void unregisterServer(String serverId) throws ServerNotRegistered;

    com.sun.corba.se.PortableActivationIDL.RepositoryPackage.ServerDef getServer(String serverId) throws ServerNotRegistered;

    boolean isInstalled(String serverId) throws ServerNotRegistered;

    void install(String serverId) throws ServerNotRegistered, com.sun.corba.se.PortableActivationIDL.ServerAlreadyInstalled;

    void uninstall(String serverId) throws ServerNotRegistered, com.sun.corba.se.PortableActivationIDL.ServerAlreadyUninstalled;

    String[] listRegisteredServers();

    String[] getApplicationNames();

    String getServerID(String applicationName) throws ServerNotRegistered;
} // interface RepositoryOperations
