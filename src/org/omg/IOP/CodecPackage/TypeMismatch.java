package org.omg.IOP.CodecPackage;

public final class TypeMismatch extends org.omg.CORBA.UserException{
    public TypeMismatch(){
        super(TypeMismatchHelper.id());
    } // ctor

    public TypeMismatch(String $reason){
        super(TypeMismatchHelper.id()+"  "+$reason);
    } // ctor
} // class TypeMismatch
