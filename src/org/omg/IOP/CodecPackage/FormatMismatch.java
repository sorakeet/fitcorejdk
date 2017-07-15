package org.omg.IOP.CodecPackage;

public final class FormatMismatch extends org.omg.CORBA.UserException{
    public FormatMismatch(){
        super(FormatMismatchHelper.id());
    } // ctor

    public FormatMismatch(String $reason){
        super(FormatMismatchHelper.id()+"  "+$reason);
    } // ctor
} // class FormatMismatch
