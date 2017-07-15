package org.omg.IOP.CodecFactoryPackage;

public final class UnknownEncoding extends org.omg.CORBA.UserException{
    public UnknownEncoding(){
        super(UnknownEncodingHelper.id());
    } // ctor

    public UnknownEncoding(String $reason){
        super(UnknownEncodingHelper.id()+"  "+$reason);
    } // ctor
} // class UnknownEncoding
