package com.sun.corba.se.spi.activation;

public final class ServerNotRegistered extends org.omg.CORBA.UserException{
    public int serverId=(int)0;

    public ServerNotRegistered(){
        super(ServerNotRegisteredHelper.id());
    } // ctor

    public ServerNotRegistered(int _serverId){
        super(ServerNotRegisteredHelper.id());
        serverId=_serverId;
    } // ctor

    public ServerNotRegistered(String $reason,int _serverId){
        super(ServerNotRegisteredHelper.id()+"  "+$reason);
        serverId=_serverId;
    } // ctor
} // class ServerNotRegistered
