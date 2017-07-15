package com.sun.corba.se.PortableActivationIDL;

public final class ServerNotRegistered extends org.omg.CORBA.UserException{
    public String serverId=null;

    public ServerNotRegistered(){
        super(ServerNotRegisteredHelper.id());
    } // ctor

    public ServerNotRegistered(String _serverId){
        super(ServerNotRegisteredHelper.id());
        serverId=_serverId;
    } // ctor

    public ServerNotRegistered(String $reason,String _serverId){
        super(ServerNotRegisteredHelper.id()+"  "+$reason);
        serverId=_serverId;
    } // ctor
} // class ServerNotRegistered
