/**
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.encoding;

import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.org.omg.SendingContext.CodeBase;
import org.omg.CORBA.CompletionStatus;
import sun.corba.EncapsInputStreamFactory;

import java.nio.ByteBuffer;

public class EncapsInputStream extends CDRInputStream{
    private ORBUtilSystemException wrapper;
    private CodeBase codeBase;

    public EncapsInputStream(org.omg.CORBA.ORB orb,ByteBuffer byteBuffer,
                             int size,boolean littleEndian,
                             GIOPVersion version){
        super(orb,byteBuffer,size,littleEndian,
                version,Message.CDR_ENC_VERSION,
                BufferManagerFactory.newBufferManagerRead(
                        BufferManagerFactory.GROW,
                        Message.CDR_ENC_VERSION,
                        (ORB)orb));
        performORBVersionSpecificInit();
    }

    // ior/IdentifiableBase
    // ior/IIOPProfile
    // corba/ORBSingleton
    // iiop/ORB
    public EncapsInputStream(org.omg.CORBA.ORB orb,byte[] data,int size){
        this(orb,data,size,GIOPVersion.V1_2);
    }

    // CDREncapsCodec
    // ServiceContext
    //
    // Assumes big endian (can use consumeEndian to read and set
    // the endianness if it is an encapsulation with a byte order
    // mark at the beginning)
    public EncapsInputStream(org.omg.CORBA.ORB orb,byte[] data,int size,GIOPVersion version){
        this(orb,data,size,false,version);
    }

    // corba/EncapsOutputStream
    // corba/ORBSingleton
    // iiop/ORB
    public EncapsInputStream(org.omg.CORBA.ORB orb,byte[] buf,
                             int size,boolean littleEndian,
                             GIOPVersion version){
        super(orb,ByteBuffer.wrap(buf),size,littleEndian,
                version,Message.CDR_ENC_VERSION,
                BufferManagerFactory.newBufferManagerRead(
                        BufferManagerFactory.GROW,
                        Message.CDR_ENC_VERSION,
                        (ORB)orb));
        wrapper=ORBUtilSystemException.get((ORB)orb,
                CORBALogDomains.RPC_ENCODING);
        performORBVersionSpecificInit();
    }

    // corba/AnyImpl
    public EncapsInputStream(EncapsInputStream eis){
        super(eis);
        wrapper=ORBUtilSystemException.get((ORB)(eis.orb()),
                CORBALogDomains.RPC_ENCODING);
        performORBVersionSpecificInit();
    }

    public EncapsInputStream(org.omg.CORBA.ORB orb,
                             byte[] data,
                             int size,
                             GIOPVersion version,
                             CodeBase codeBase){
        super(orb,
                ByteBuffer.wrap(data),
                size,
                false,
                version,Message.CDR_ENC_VERSION,
                BufferManagerFactory.newBufferManagerRead(
                        BufferManagerFactory.GROW,
                        Message.CDR_ENC_VERSION,
                        (ORB)orb));
        this.codeBase=codeBase;
        performORBVersionSpecificInit();
    }

    public CDRInputStream dup(){
        return EncapsInputStreamFactory.newEncapsInputStream(this);
    }

    public CodeBase getCodeBase(){
        return codeBase;
    }

    protected CodeSetConversion.BTCConverter createCharBTCConverter(){
        return CodeSetConversion.impl().getBTCConverter(OSFCodeSetRegistry.ISO_8859_1);
    }

    protected CodeSetConversion.BTCConverter createWCharBTCConverter(){
        // Wide characters don't exist in GIOP 1.0
        if(getGIOPVersion().equals(GIOPVersion.V1_0))
            throw wrapper.wcharDataInGiop10(CompletionStatus.COMPLETED_MAYBE);
        // In GIOP 1.1, we shouldn't have byte order markers.  Take the order
        // of the stream if we don't see them.
        if(getGIOPVersion().equals(GIOPVersion.V1_1))
            return CodeSetConversion.impl().getBTCConverter(OSFCodeSetRegistry.UTF_16,
                    isLittleEndian());
        // Assume anything else adheres to GIOP 1.2 requirements.
        //
        // Our UTF_16 converter will work with byte order markers, and if
        // they aren't present, it will use the provided endianness.
        //
        // With no byte order marker, it's big endian in GIOP 1.2.
        // formal 00-11-03 15.3.16.
        return CodeSetConversion.impl().getBTCConverter(OSFCodeSetRegistry.UTF_16,
                false);
    }
}
