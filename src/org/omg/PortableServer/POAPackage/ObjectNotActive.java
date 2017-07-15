package org.omg.PortableServer.POAPackage;

public final class ObjectNotActive extends org.omg.CORBA.UserException{
    public ObjectNotActive(){
        super(ObjectNotActiveHelper.id());
    } // ctor

    public ObjectNotActive(String $reason){
        super(ObjectNotActiveHelper.id()+"  "+$reason);
    } // ctor
} // class ObjectNotActive
