package org.omg.PortableServer.POAPackage;

public final class ServantNotActive extends org.omg.CORBA.UserException{
    public ServantNotActive(){
        super(ServantNotActiveHelper.id());
    } // ctor

    public ServantNotActive(String $reason){
        super(ServantNotActiveHelper.id()+"  "+$reason);
    } // ctor
} // class ServantNotActive
