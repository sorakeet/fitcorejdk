package org.omg.DynamicAny.DynAnyFactoryPackage;

public final class InconsistentTypeCode extends org.omg.CORBA.UserException{
    public InconsistentTypeCode(){
        super(InconsistentTypeCodeHelper.id());
    } // ctor

    public InconsistentTypeCode(String $reason){
        super(InconsistentTypeCodeHelper.id()+"  "+$reason);
    } // ctor
} // class InconsistentTypeCode
