package com.sun.corba.se.PortableActivationIDL;

public interface ActivatorOperations{
    void registerServer(String serverId,ServerProxy serverObj) throws com.sun.corba.se.PortableActivationIDL.ServerNotRegistered;

    void serverGoingDown(String serverId);

    void registerORB(String serverId,String orbId,ORBProxy orb,EndPointInfo[] endPointInfo) throws com.sun.corba.se.PortableActivationIDL.ServerNotRegistered, NoSuchEndPoint, com.sun.corba.se.PortableActivationIDL.ORBAlreadyRegistered;

    org.omg.PortableInterceptor.ObjectReferenceTemplate registerPOA(String serverId,String orbId,org.omg.PortableInterceptor.ObjectReferenceTemplate poaTemplate);

    void poaDestroyed(String serverId,String orbId,org.omg.PortableInterceptor.ObjectReferenceTemplate poaTemplate);

    void activate(String serverId) throws com.sun.corba.se.PortableActivationIDL.ServerAlreadyActive, com.sun.corba.se.PortableActivationIDL.ServerNotRegistered, ServerHeldDown;

    void shutdown(String serverId) throws ServerNotActive, com.sun.corba.se.PortableActivationIDL.ServerNotRegistered;

    void install(String serverId) throws com.sun.corba.se.PortableActivationIDL.ServerNotRegistered, ServerHeldDown, com.sun.corba.se.PortableActivationIDL.ServerAlreadyInstalled;

    void uninstall(String serverId) throws com.sun.corba.se.PortableActivationIDL.ServerNotRegistered, ServerHeldDown, com.sun.corba.se.PortableActivationIDL.ServerAlreadyUninstalled;

    String[] getActiveServers();

    String[] getORBNames(String serverId) throws com.sun.corba.se.PortableActivationIDL.ServerNotRegistered;

    org.omg.PortableInterceptor.ObjectReferenceTemplate lookupPOATemplate(String serverId,String orbId,String[] orbAdapterName);
} // interface ActivatorOperations
