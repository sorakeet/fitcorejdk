package org.omg.PortableServer.POAPackage;

public final class ServantAlreadyActive extends org.omg.CORBA.UserException{
    public ServantAlreadyActive(){
        super(ServantAlreadyActiveHelper.id());
    } // ctor

    public ServantAlreadyActive(String $reason){
        super(ServantAlreadyActiveHelper.id()+"  "+$reason);
    } // ctor
} // class ServantAlreadyActive
