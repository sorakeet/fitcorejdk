package com.sun.corba.se.PortableActivationIDL;

public final class ServerHeldDown extends org.omg.CORBA.UserException{
    public String serverId=null;

    public ServerHeldDown(){
        super(ServerHeldDownHelper.id());
    } // ctor

    public ServerHeldDown(String _serverId){
        super(ServerHeldDownHelper.id());
        serverId=_serverId;
    } // ctor

    public ServerHeldDown(String $reason,String _serverId){
        super(ServerHeldDownHelper.id()+"  "+$reason);
        serverId=_serverId;
    } // ctor
} // class ServerHeldDown
