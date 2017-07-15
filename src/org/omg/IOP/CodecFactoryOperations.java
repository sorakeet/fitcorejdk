package org.omg.IOP;

public interface CodecFactoryOperations{
    Codec create_codec(Encoding enc) throws org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
} // interface CodecFactoryOperations
