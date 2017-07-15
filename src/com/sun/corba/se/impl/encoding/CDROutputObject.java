/**
 * Copyright (c) 2001, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.encoding;

import com.sun.corba.se.impl.logging.OMGSystemException;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.se.pept.protocol.MessageMediator;
import com.sun.corba.se.spi.encoding.CorbaOutputObject;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;
import com.sun.corba.se.spi.transport.CorbaConnection;
import org.omg.CORBA.portable.InputStream;

import java.io.IOException;

public class CDROutputObject extends CorbaOutputObject{
    private Message header;
    private ORB orb;
    private ORBUtilSystemException wrapper;
    private OMGSystemException omgWrapper;
    // REVISIT - only used on sendCancelRequest.
    private CorbaConnection connection;

    public CDROutputObject(ORB orb,
                           MessageMediator messageMediator,
                           Message header,
                           byte streamFormatVersion){
        this(
                orb,
                ((CorbaMessageMediator)messageMediator).getGIOPVersion(),
                header,
                BufferManagerFactory.newBufferManagerWrite(
                        ((CorbaMessageMediator)messageMediator).getGIOPVersion(),
                        header.getEncodingVersion(),
                        orb),
                streamFormatVersion,
                (CorbaMessageMediator)messageMediator);
    }

    private CDROutputObject(
            ORB orb,GIOPVersion giopVersion,Message header,
            BufferManagerWrite manager,byte streamFormatVersion,
            CorbaMessageMediator mediator){
        super(orb,giopVersion,header.getEncodingVersion(),
                false,manager,streamFormatVersion,
                ((mediator!=null&&mediator.getConnection()!=null)?
                        ((CorbaConnection)mediator.getConnection()).
                                shouldUseDirectByteBuffers():false));
        this.header=header;
        this.orb=orb;
        this.wrapper=ORBUtilSystemException.get(orb,CORBALogDomains.RPC_ENCODING);
        this.omgWrapper=OMGSystemException.get(orb,CORBALogDomains.RPC_ENCODING);
        getBufferManager().setOutputObject(this);
        this.corbaMessageMediator=mediator;
    }

    // NOTE:
    // Used in SharedCDR (i.e., must be grow).
    // Used in msgtypes test.
    public CDROutputObject(ORB orb,
                           MessageMediator messageMediator,
                           Message header,
                           byte streamFormatVersion,
                           int strategy){
        this(
                orb,
                ((CorbaMessageMediator)messageMediator).getGIOPVersion(),
                header,
                BufferManagerFactory.
                        newBufferManagerWrite(strategy,
                                header.getEncodingVersion(),
                                orb),
                streamFormatVersion,
                (CorbaMessageMediator)messageMediator);
    }

    // REVISIT
    // Used on sendCancelRequest.
    // Used for needs addressing mode.
    public CDROutputObject(ORB orb,CorbaMessageMediator mediator,
                           GIOPVersion giopVersion,
                           CorbaConnection connection,Message header,
                           byte streamFormatVersion){
        this(
                orb,
                giopVersion,
                header,
                BufferManagerFactory.
                        newBufferManagerWrite(giopVersion,
                                header.getEncodingVersion(),
                                orb),
                streamFormatVersion,
                mediator);
        this.connection=connection;
    }

    public final void finishSendingMessage(){
        getBufferManager().sendMessage();
    }

    public void writeTo(CorbaConnection connection)
            throws IOException{
        //
        // Update the GIOP MessageHeader size field.
        //
        ByteBufferWithInfo bbwi=getByteBufferWithInfo();
        getMessageHeader().setSize(bbwi.byteBuffer,bbwi.getSize());
        if(orb()!=null){
            if(((ORB)orb()).transportDebugFlag){
                dprint(".writeTo: "+connection);
            }
            if(((ORB)orb()).giopDebugFlag){
                CDROutputStream_1_0.printBuffer(bbwi);
            }
        }
        bbwi.byteBuffer.position(0).limit(bbwi.getSize());
        connection.write(bbwi.byteBuffer);
    }

    // XREVISIT
    // Header should only be in message mediator.
    // Another possibility: merge header and message mediator.
    // REVISIT - make protected once all encoding together
    public Message getMessageHeader(){
        return header;
    }

    protected void dprint(String msg){
        ORBUtility.dprint("CDROutputObject",msg);
    }

    public InputStream create_input_stream(){
        // XREVISIT
        return null;
        //return new XIIOPInputStream(orb(), getByteBuffer(), getIndex(),
        //isLittleEndian(), getMessageHeader(), conn);
    }

    // XREVISIT - If CDROutputObject doesn't live in the iiop
    // package, it will need this, here, to give package access
    // to xgiop.
    // REVISIT - make protected once all encoding together
    public final ByteBufferWithInfo getByteBufferWithInfo(){
        return super.getByteBufferWithInfo();
    }

    // REVISIT - make protected once all encoding together
    public final void setByteBufferWithInfo(ByteBufferWithInfo bbwi){
        super.setByteBufferWithInfo(bbwi);
    }

    protected CodeSetConversion.CTBConverter createCharCTBConverter(){
        CodeSetComponentInfo.CodeSetContext codesets=getCodeSets();
        // If the connection doesn't have its negotiated
        // code sets by now, fall back on the defaults defined
        // in CDRInputStream.
        if(codesets==null)
            return super.createCharCTBConverter();
        OSFCodeSetRegistry.Entry charSet
                =OSFCodeSetRegistry.lookupEntry(codesets.getCharCodeSet());
        if(charSet==null)
            throw wrapper.unknownCodeset(charSet);
        return CodeSetConversion.impl().getCTBConverter(charSet,
                isLittleEndian(),
                false);
    }

    protected CodeSetConversion.CTBConverter createWCharCTBConverter(){
        CodeSetComponentInfo.CodeSetContext codesets=getCodeSets();
        // If the connection doesn't have its negotiated
        // code sets by now, we have to throw an exception.
        // See CORBA formal 00-11-03 13.9.2.6.
        if(codesets==null){
            if(getConnection().isServer())
                throw omgWrapper.noClientWcharCodesetCtx();
            else
                throw omgWrapper.noServerWcharCodesetCmp();
        }
        OSFCodeSetRegistry.Entry wcharSet
                =OSFCodeSetRegistry.lookupEntry(codesets.getWCharCodeSet());
        if(wcharSet==null)
            throw wrapper.unknownCodeset(wcharSet);
        boolean useByteOrderMarkers
                =((ORB)orb()).getORBData().useByteOrderMarkers();
        // With UTF-16:
        //
        // For GIOP 1.2, we can put byte order markers if we want to, and
        // use the default of big endian otherwise.  (See issue 3405b)
        //
        // For GIOP 1.1, we don't use BOMs and use the endianness of
        // the stream.
        if(wcharSet==OSFCodeSetRegistry.UTF_16){
            if(getGIOPVersion().equals(GIOPVersion.V1_2)){
                return CodeSetConversion.impl().getCTBConverter(wcharSet,
                        false,
                        useByteOrderMarkers);
            }
            if(getGIOPVersion().equals(GIOPVersion.V1_1)){
                return CodeSetConversion.impl().getCTBConverter(wcharSet,
                        isLittleEndian(),
                        false);
            }
        }
        // In the normal case, let the converter system handle it
        return CodeSetConversion.impl().getCTBConverter(wcharSet,
                isLittleEndian(),
                useByteOrderMarkers);
    }

    // If we're local and don't have a Connection, use the
    // local code sets, otherwise get them from the connection.
    // If the connection doesn't have negotiated code sets
    // yet, then we use ISO8859-1 for char/string and wchar/wstring
    // are illegal.
    private CodeSetComponentInfo.CodeSetContext getCodeSets(){
        if(getConnection()==null)
            return CodeSetComponentInfo.LOCAL_CODE_SETS;
        else
            return getConnection().getCodeSetContext();
    }

    public CorbaConnection getConnection(){
        // REVISIT - only set when doing sendCancelRequest.
        if(connection!=null){
            return connection;
        }
        return (CorbaConnection)corbaMessageMediator.getConnection();
    }
}
// End of file.
