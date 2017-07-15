package org.omg.PortableInterceptor.ORBInitInfoPackage;

public final class DuplicateName extends org.omg.CORBA.UserException{
    public String name=null;

    public DuplicateName(){
        super(DuplicateNameHelper.id());
    } // ctor

    public DuplicateName(String _name){
        super(DuplicateNameHelper.id());
        name=_name;
    } // ctor

    public DuplicateName(String $reason,String _name){
        super(DuplicateNameHelper.id()+"  "+$reason);
        name=_name;
    } // ctor
} // class DuplicateName
