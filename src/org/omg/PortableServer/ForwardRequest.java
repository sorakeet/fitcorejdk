package org.omg.PortableServer;

public final class ForwardRequest extends org.omg.CORBA.UserException{
    public org.omg.CORBA.Object forward_reference=null;

    public ForwardRequest(){
        super(ForwardRequestHelper.id());
    } // ctor

    public ForwardRequest(org.omg.CORBA.Object _forward_reference){
        super(ForwardRequestHelper.id());
        forward_reference=_forward_reference;
    } // ctor

    public ForwardRequest(String $reason,org.omg.CORBA.Object _forward_reference){
        super(ForwardRequestHelper.id()+"  "+$reason);
        forward_reference=_forward_reference;
    } // ctor
} // class ForwardRequest
