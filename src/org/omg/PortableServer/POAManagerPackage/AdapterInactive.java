package org.omg.PortableServer.POAManagerPackage;

public final class AdapterInactive extends org.omg.CORBA.UserException{
    public AdapterInactive(){
        super(AdapterInactiveHelper.id());
    } // ctor

    public AdapterInactive(String $reason){
        super(AdapterInactiveHelper.id()+"  "+$reason);
    } // ctor
} // class AdapterInactive
