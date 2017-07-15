package org.omg.PortableServer.POAPackage;

public final class AdapterAlreadyExists extends org.omg.CORBA.UserException{
    public AdapterAlreadyExists(){
        super(AdapterAlreadyExistsHelper.id());
    } // ctor

    public AdapterAlreadyExists(String $reason){
        super(AdapterAlreadyExistsHelper.id()+"  "+$reason);
    } // ctor
} // class AdapterAlreadyExists
