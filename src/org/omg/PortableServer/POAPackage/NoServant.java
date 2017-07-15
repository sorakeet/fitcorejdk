package org.omg.PortableServer.POAPackage;

public final class NoServant extends org.omg.CORBA.UserException{
    public NoServant(){
        super(NoServantHelper.id());
    } // ctor

    public NoServant(String $reason){
        super(NoServantHelper.id()+"  "+$reason);
    } // ctor
} // class NoServant
