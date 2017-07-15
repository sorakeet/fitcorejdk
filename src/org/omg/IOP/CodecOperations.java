package org.omg.IOP;

public interface CodecOperations{
    byte[] encode(org.omg.CORBA.Any data) throws org.omg.IOP.CodecPackage.InvalidTypeForEncoding;

    org.omg.CORBA.Any decode(byte[] data) throws org.omg.IOP.CodecPackage.FormatMismatch;

    byte[] encode_value(org.omg.CORBA.Any data) throws org.omg.IOP.CodecPackage.InvalidTypeForEncoding;

    org.omg.CORBA.Any decode_value(byte[] data,org.omg.CORBA.TypeCode tc) throws org.omg.IOP.CodecPackage.FormatMismatch, org.omg.IOP.CodecPackage.TypeMismatch;
} // interface CodecOperations
