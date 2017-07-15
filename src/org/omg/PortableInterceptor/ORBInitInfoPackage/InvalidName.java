package org.omg.PortableInterceptor.ORBInitInfoPackage;

public final class InvalidName extends org.omg.CORBA.UserException{
    public InvalidName(){
        super(InvalidNameHelper.id());
    } // ctor

    public InvalidName(String $reason){
        super(InvalidNameHelper.id()+"  "+$reason);
    } // ctor
} // class InvalidName
