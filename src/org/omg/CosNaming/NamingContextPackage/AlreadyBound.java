package org.omg.CosNaming.NamingContextPackage;

public final class AlreadyBound extends org.omg.CORBA.UserException{
    public AlreadyBound(){
        super(AlreadyBoundHelper.id());
    } // ctor

    public AlreadyBound(String $reason){
        super(AlreadyBoundHelper.id()+"  "+$reason);
    } // ctor
} // class AlreadyBound
