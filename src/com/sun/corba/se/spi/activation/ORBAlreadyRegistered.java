package com.sun.corba.se.spi.activation;

public final class ORBAlreadyRegistered extends org.omg.CORBA.UserException{
    public String orbId=null;

    public ORBAlreadyRegistered(){
        super(ORBAlreadyRegisteredHelper.id());
    } // ctor

    public ORBAlreadyRegistered(String _orbId){
        super(ORBAlreadyRegisteredHelper.id());
        orbId=_orbId;
    } // ctor

    public ORBAlreadyRegistered(String $reason,String _orbId){
        super(ORBAlreadyRegisteredHelper.id()+"  "+$reason);
        orbId=_orbId;
    } // ctor
} // class ORBAlreadyRegistered
