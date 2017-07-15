package org.omg.IOP;

public final class Encoding implements org.omg.CORBA.portable.IDLEntity{
    public short format=(short)0;
    public byte major_version=(byte)0;
    public byte minor_version=(byte)0;

    public Encoding(){
    } // ctor

    public Encoding(short _format,byte _major_version,byte _minor_version){
        format=_format;
        major_version=_major_version;
        minor_version=_minor_version;
    } // ctor
} // class Encoding
