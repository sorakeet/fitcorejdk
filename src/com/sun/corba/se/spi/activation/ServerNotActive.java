package com.sun.corba.se.spi.activation;

public final class ServerNotActive extends org.omg.CORBA.UserException{
    public int serverId=(int)0;

    public ServerNotActive(){
        super(ServerNotActiveHelper.id());
    } // ctor

    public ServerNotActive(int _serverId){
        super(ServerNotActiveHelper.id());
        serverId=_serverId;
    } // ctor

    public ServerNotActive(String $reason,int _serverId){
        super(ServerNotActiveHelper.id()+"  "+$reason);
        serverId=_serverId;
    } // ctor
} // class ServerNotActive
