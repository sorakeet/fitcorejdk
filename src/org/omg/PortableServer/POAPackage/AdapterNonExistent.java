package org.omg.PortableServer.POAPackage;

public final class AdapterNonExistent extends org.omg.CORBA.UserException{
    public AdapterNonExistent(){
        super(AdapterNonExistentHelper.id());
    } // ctor

    public AdapterNonExistent(String $reason){
        super(AdapterNonExistentHelper.id()+"  "+$reason);
    } // ctor
} // class AdapterNonExistent
