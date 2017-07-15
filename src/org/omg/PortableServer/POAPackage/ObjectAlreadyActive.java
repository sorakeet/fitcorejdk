package org.omg.PortableServer.POAPackage;

public final class ObjectAlreadyActive extends org.omg.CORBA.UserException{
    public ObjectAlreadyActive(){
        super(ObjectAlreadyActiveHelper.id());
    } // ctor

    public ObjectAlreadyActive(String $reason){
        super(ObjectAlreadyActiveHelper.id()+"  "+$reason);
    } // ctor
} // class ObjectAlreadyActive
