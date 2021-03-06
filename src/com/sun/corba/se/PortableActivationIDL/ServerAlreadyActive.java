package com.sun.corba.se.PortableActivationIDL;

public final class ServerAlreadyActive extends org.omg.CORBA.UserException{
    public String serverId=null;

    public ServerAlreadyActive(){
        super(ServerAlreadyActiveHelper.id());
    } // ctor

    public ServerAlreadyActive(String _serverId){
        super(ServerAlreadyActiveHelper.id());
        serverId=_serverId;
    } // ctor

    public ServerAlreadyActive(String $reason,String _serverId){
        super(ServerAlreadyActiveHelper.id()+"  "+$reason);
        serverId=_serverId;
    } // ctor
} // class ServerAlreadyActive
