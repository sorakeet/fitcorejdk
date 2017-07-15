package org.omg.CosNaming.NamingContextPackage;

public final class InvalidName extends org.omg.CORBA.UserException{
    public InvalidName(){
        super(InvalidNameHelper.id());
    } // ctor

    public InvalidName(String $reason){
        super(InvalidNameHelper.id()+"  "+$reason);
    } // ctor
} // class InvalidName
