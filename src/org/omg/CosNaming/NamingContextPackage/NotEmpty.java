package org.omg.CosNaming.NamingContextPackage;

public final class NotEmpty extends org.omg.CORBA.UserException{
    public NotEmpty(){
        super(NotEmptyHelper.id());
    } // ctor

    public NotEmpty(String $reason){
        super(NotEmptyHelper.id()+"  "+$reason);
    } // ctor
} // class NotEmpty
