/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.interceptors;

import com.sun.corba.se.impl.corba.AnyImpl;
import com.sun.corba.se.impl.encoding.EncapsInputStream;
import com.sun.corba.se.impl.encoding.EncapsOutputStream;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.CodecPackage.TypeMismatch;
import sun.corba.EncapsInputStreamFactory;

public final class CDREncapsCodec
        extends LocalObject
        implements Codec{
    ORBUtilSystemException wrapper;
    // The ORB that created the factory this codec was created from
    private ORB orb;
    // The GIOP version we are encoding for
    private GIOPVersion giopVersion;

    public CDREncapsCodec(ORB orb,int major,int minor){
        this.orb=orb;
        wrapper=ORBUtilSystemException.get(
                (com.sun.corba.se.spi.orb.ORB)orb,CORBALogDomains.RPC_PROTOCOL);
        giopVersion=GIOPVersion.getInstance((byte)major,(byte)minor);
    }

    public byte[] encode(Any data)
            throws InvalidTypeForEncoding{
        if(data==null)
            throw wrapper.nullParam();
        return encodeImpl(data,true);
    }

    public Any decode(byte[] data)
            throws FormatMismatch{
        if(data==null)
            throw wrapper.nullParam();
        return decodeImpl(data,null);
    }

    public byte[] encode_value(Any data)
            throws InvalidTypeForEncoding{
        if(data==null)
            throw wrapper.nullParam();
        return encodeImpl(data,false);
    }

    public Any decode_value(byte[] data,TypeCode tc)
            throws FormatMismatch, TypeMismatch{
        if(data==null)
            throw wrapper.nullParam();
        if(tc==null)
            throw wrapper.nullParam();
        return decodeImpl(data,tc);
    }

    private Any decodeImpl(byte[] data,TypeCode tc)
            throws FormatMismatch{
        if(data==null)
            throw wrapper.nullParam();
        AnyImpl any=null;  // return value
        // _REVISIT_ Currently there is no way for us to distinguish between
        // a FormatMismatch and a TypeMismatch because we cannot get this
        // information from the CDRInputStream.  If a RuntimeException occurs,
        // it is turned into a FormatMismatch exception.
        try{
            EncapsInputStream cdrIn=EncapsInputStreamFactory.newEncapsInputStream(orb,data,
                    data.length,giopVersion);
            cdrIn.consumeEndian();
            // If type code not specified, read it from octet stream:
            if(tc==null){
                tc=cdrIn.read_TypeCode();
            }
            // Create a new Any object:
            any=new AnyImpl((com.sun.corba.se.spi.orb.ORB)orb);
            any.read_value(cdrIn,tc);
        }catch(RuntimeException e){
            // See above note.
            throw new FormatMismatch();
        }
        return any;
    }

    private byte[] encodeImpl(Any data,boolean sendTypeCode)
            throws InvalidTypeForEncoding{
        if(data==null)
            throw wrapper.nullParam();
        // _REVISIT_ Note that InvalidTypeForEncoding is never thrown in
        // the body of this method.  This is due to the fact that CDR*Stream
        // will never throw an exception if the encoding is invalid.  To
        // fix this, the CDROutputStream must know the version of GIOP it
        // is encoding for and it must check to ensure that, for example,
        // wstring cannot be encoded in GIOP 1.0.
        //
        // As part of the GIOP 1.2 work, the CDRInput and OutputStream will
        // be versioned.  This can be handled once this work is complete.
        // Create output stream with default endianness.
        EncapsOutputStream cdrOut=
                sun.corba.OutputStreamFactory.newEncapsOutputStream(
                        (com.sun.corba.se.spi.orb.ORB)orb,giopVersion);
        // This is an encapsulation, so put out the endian:
        cdrOut.putEndian();
        // Sometimes encode type code:
        if(sendTypeCode){
            cdrOut.write_TypeCode(data.type());
        }
        // Encode value and return.
        data.write_value(cdrOut);
        return cdrOut.toByteArray();
    }
}
