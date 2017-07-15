package com.sun.corba.se.PortableActivationIDL;

public final class BadServerDefinition extends org.omg.CORBA.UserException{
    public String reason=null;

    public BadServerDefinition(){
        super(BadServerDefinitionHelper.id());
    } // ctor

    public BadServerDefinition(String _reason){
        super(BadServerDefinitionHelper.id());
        reason=_reason;
    } // ctor

    public BadServerDefinition(String $reason,String _reason){
        super(BadServerDefinitionHelper.id()+"  "+$reason);
        reason=_reason;
    } // ctor
} // class BadServerDefinition
