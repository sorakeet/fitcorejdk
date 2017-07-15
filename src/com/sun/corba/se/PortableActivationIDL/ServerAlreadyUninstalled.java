package com.sun.corba.se.PortableActivationIDL;

public final class ServerAlreadyUninstalled extends org.omg.CORBA.UserException{
    public String serverId=null;

    public ServerAlreadyUninstalled(){
        super(ServerAlreadyUninstalledHelper.id());
    } // ctor

    public ServerAlreadyUninstalled(String _serverId){
        super(ServerAlreadyUninstalledHelper.id());
        serverId=_serverId;
    } // ctor

    public ServerAlreadyUninstalled(String $reason,String _serverId){
        super(ServerAlreadyUninstalledHelper.id()+"  "+$reason);
        serverId=_serverId;
    } // ctor
} // class ServerAlreadyUninstalled
