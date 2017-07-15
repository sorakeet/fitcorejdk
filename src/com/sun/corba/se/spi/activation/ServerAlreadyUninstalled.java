package com.sun.corba.se.spi.activation;

public final class ServerAlreadyUninstalled extends org.omg.CORBA.UserException{
    public int serverId=(int)0;

    public ServerAlreadyUninstalled(){
        super(ServerAlreadyUninstalledHelper.id());
    } // ctor

    public ServerAlreadyUninstalled(int _serverId){
        super(ServerAlreadyUninstalledHelper.id());
        serverId=_serverId;
    } // ctor

    public ServerAlreadyUninstalled(String $reason,int _serverId){
        super(ServerAlreadyUninstalledHelper.id()+"  "+$reason);
        serverId=_serverId;
    } // ctor
} // class ServerAlreadyUninstalled
