package com.sun.corba.se.spi.activation;

public final class ServerAlreadyActive extends org.omg.CORBA.UserException{
    public int serverId=(int)0;

    public ServerAlreadyActive(){
        super(ServerAlreadyActiveHelper.id());
    } // ctor

    public ServerAlreadyActive(int _serverId){
        super(ServerAlreadyActiveHelper.id());
        serverId=_serverId;
    } // ctor

    public ServerAlreadyActive(String $reason,int _serverId){
        super(ServerAlreadyActiveHelper.id()+"  "+$reason);
        serverId=_serverId;
    } // ctor
} // class ServerAlreadyActive
