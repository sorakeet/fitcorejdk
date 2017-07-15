package org.omg.PortableInterceptor;

public final class ForwardRequest extends org.omg.CORBA.UserException{
    public org.omg.CORBA.Object forward=null;

    public ForwardRequest(){
        super(ForwardRequestHelper.id());
    } // ctor

    public ForwardRequest(org.omg.CORBA.Object _forward){
        super(ForwardRequestHelper.id());
        forward=_forward;
    } // ctor

    public ForwardRequest(String $reason,org.omg.CORBA.Object _forward){
        super(ForwardRequestHelper.id()+"  "+$reason);
        forward=_forward;
    } // ctor
} // class ForwardRequest
