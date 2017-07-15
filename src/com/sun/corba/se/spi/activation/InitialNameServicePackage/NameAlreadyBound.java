package com.sun.corba.se.spi.activation.InitialNameServicePackage;

public final class NameAlreadyBound extends org.omg.CORBA.UserException{
    public NameAlreadyBound(){
        super(NameAlreadyBoundHelper.id());
    } // ctor

    public NameAlreadyBound(String $reason){
        super(NameAlreadyBoundHelper.id()+"  "+$reason);
    } // ctor
} // class NameAlreadyBound
