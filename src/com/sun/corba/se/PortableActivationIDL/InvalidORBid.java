package com.sun.corba.se.PortableActivationIDL;

public final class InvalidORBid extends org.omg.CORBA.UserException{
    public InvalidORBid(){
        super(InvalidORBidHelper.id());
    } // ctor

    public InvalidORBid(String $reason){
        super(InvalidORBidHelper.id()+"  "+$reason);
    } // ctor
} // class InvalidORBid
