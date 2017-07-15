package org.omg.CosNaming.NamingContextPackage;

public final class NotFound extends org.omg.CORBA.UserException{
    public NotFoundReason why=null;
    public org.omg.CosNaming.NameComponent rest_of_name[]=null;

    public NotFound(){
        super(NotFoundHelper.id());
    } // ctor

    public NotFound(NotFoundReason _why,org.omg.CosNaming.NameComponent[] _rest_of_name){
        super(NotFoundHelper.id());
        why=_why;
        rest_of_name=_rest_of_name;
    } // ctor

    public NotFound(String $reason,NotFoundReason _why,org.omg.CosNaming.NameComponent[] _rest_of_name){
        super(NotFoundHelper.id()+"  "+$reason);
        why=_why;
        rest_of_name=_rest_of_name;
    } // ctor
} // class NotFound
