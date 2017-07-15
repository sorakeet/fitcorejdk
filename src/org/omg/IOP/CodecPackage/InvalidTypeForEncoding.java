package org.omg.IOP.CodecPackage;

public final class InvalidTypeForEncoding extends org.omg.CORBA.UserException{
    public InvalidTypeForEncoding(){
        super(InvalidTypeForEncodingHelper.id());
    } // ctor

    public InvalidTypeForEncoding(String $reason){
        super(InvalidTypeForEncodingHelper.id()+"  "+$reason);
    } // ctor
} // class InvalidTypeForEncoding
