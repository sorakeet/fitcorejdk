/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.interceptors;

import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;

public final class CodecFactoryImpl
        extends LocalObject
        implements CodecFactory{
    // The maximum minor version of GIOP supported by this codec factory.
    // Currently, this is 1.2.
    private static final int MAX_MINOR_VERSION_SUPPORTED=2;
    // The ORB that created this Codec Factory
    private ORB orb;
    private ORBUtilSystemException wrapper;
    // The pre-created minor versions of Codec version 1.0, 1.1, ...,
    // 1.(MAX_MINOR_VERSION_SUPPORTED)
    private Codec codecs[]=new Codec[MAX_MINOR_VERSION_SUPPORTED+1];

    public CodecFactoryImpl(ORB orb){
        this.orb=orb;
        wrapper=ORBUtilSystemException.get(
                (com.sun.corba.se.spi.orb.ORB)orb,
                CORBALogDomains.RPC_PROTOCOL);
        // Precreate a codec for version 1.0 through
        // 1.(MAX_MINOR_VERSION_SUPPORTED).  This can be
        // done since Codecs are immutable in their current implementation.
        // This is an optimization that eliminates the overhead of creating
        // a new Codec each time create_codec is called.
        for(int minor=0;minor<=MAX_MINOR_VERSION_SUPPORTED;minor++){
            codecs[minor]=new CDREncapsCodec(orb,1,minor);
        }
    }

    public Codec create_codec(Encoding enc)
            throws UnknownEncoding{
        if(enc==null) nullParam();
        Codec result=null;
        // This is the only format we can currently create codecs for:
        if((enc.format==ENCODING_CDR_ENCAPS.value)&&
                (enc.major_version==1)){
            if((enc.minor_version>=0)&&
                    (enc.minor_version<=MAX_MINOR_VERSION_SUPPORTED)){
                result=codecs[enc.minor_version];
            }
        }
        if(result==null){
            throw new UnknownEncoding();
        }
        return result;
    }

    private void nullParam(){
        throw wrapper.nullParam();
    }
}
