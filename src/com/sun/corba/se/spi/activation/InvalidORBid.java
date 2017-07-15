package com.sun.corba.se.spi.activation;

public final class InvalidORBid extends org.omg.CORBA.UserException{
    public InvalidORBid(){
        super(InvalidORBidHelper.id());
    } // ctor

    public InvalidORBid(String $reason){
        super(InvalidORBidHelper.id()+"  "+$reason);
    } // ctor
} // class InvalidORBid
