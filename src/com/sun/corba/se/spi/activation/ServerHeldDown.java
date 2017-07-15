package com.sun.corba.se.spi.activation;

public final class ServerHeldDown extends org.omg.CORBA.UserException{
    public int serverId=(int)0;

    public ServerHeldDown(){
        super(ServerHeldDownHelper.id());
    } // ctor

    public ServerHeldDown(int _serverId){
        super(ServerHeldDownHelper.id());
        serverId=_serverId;
    } // ctor

    public ServerHeldDown(String $reason,int _serverId){
        super(ServerHeldDownHelper.id()+"  "+$reason);
        serverId=_serverId;
    } // ctor
} // class ServerHeldDown
