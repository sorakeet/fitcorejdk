package com.sun.corba.se.PortableActivationIDL;

public final class NoSuchEndPoint extends org.omg.CORBA.UserException{
    public NoSuchEndPoint(){
        super(NoSuchEndPointHelper.id());
    } // ctor

    public NoSuchEndPoint(String $reason){
        super(NoSuchEndPointHelper.id()+"  "+$reason);
    } // ctor
} // class NoSuchEndPoint
