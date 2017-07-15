package com.sun.corba.se.PortableActivationIDL;

public final class ServerAlreadyRegistered extends org.omg.CORBA.UserException{
    public String serverId=null;

    public ServerAlreadyRegistered(){
        super(ServerAlreadyRegisteredHelper.id());
    } // ctor

    public ServerAlreadyRegistered(String _serverId){
        super(ServerAlreadyRegisteredHelper.id());
        serverId=_serverId;
    } // ctor

    public ServerAlreadyRegistered(String $reason,String _serverId){
        super(ServerAlreadyRegisteredHelper.id()+"  "+$reason);
        serverId=_serverId;
    } // ctor
} // class ServerAlreadyRegistered
