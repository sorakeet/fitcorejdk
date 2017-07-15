package org.omg.PortableInterceptor;

public final class InvalidSlot extends org.omg.CORBA.UserException{
    public InvalidSlot(){
        super(InvalidSlotHelper.id());
    } // ctor

    public InvalidSlot(String $reason){
        super(InvalidSlotHelper.id()+"  "+$reason);
    } // ctor
} // class InvalidSlot
