/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.ietf.jgss;

import java.io.InputStream;
import java.io.OutputStream;

public interface GSSContext{
    public static final int DEFAULT_LIFETIME=0;
    public static final int INDEFINITE_LIFETIME=Integer.MAX_VALUE;

    public byte[] initSecContext(byte inputBuf[],int offset,int len)
            throws GSSException;

    public int initSecContext(InputStream inStream,
                              OutputStream outStream) throws GSSException;

    public byte[] acceptSecContext(byte inToken[],int offset,int len)
            throws GSSException;

    public void acceptSecContext(InputStream inStream,
                                 OutputStream outStream) throws GSSException;

    public boolean isEstablished();

    public void dispose() throws GSSException;

    public int getWrapSizeLimit(int qop,boolean confReq,
                                int maxTokenSize) throws GSSException;

    public byte[] wrap(byte inBuf[],int offset,int len,
                       MessageProp msgProp) throws GSSException;

    public void wrap(InputStream inStream,OutputStream outStream,
                     MessageProp msgProp) throws GSSException;

    public byte[] unwrap(byte[] inBuf,int offset,int len,
                         MessageProp msgProp) throws GSSException;

    public void unwrap(InputStream inStream,OutputStream outStream,
                       MessageProp msgProp) throws GSSException;

    public byte[] getMIC(byte[] inMsg,int offset,int len,
                         MessageProp msgProp) throws GSSException;

    public void getMIC(InputStream inStream,OutputStream outStream,
                       MessageProp msgProp) throws GSSException;

    public void verifyMIC(byte[] inToken,int tokOffset,int tokLen,
                          byte[] inMsg,int msgOffset,int msgLen,
                          MessageProp msgProp) throws GSSException;

    public void verifyMIC(InputStream tokStream,InputStream msgStream,
                          MessageProp msgProp) throws GSSException;

    public byte[] export() throws GSSException;

    public void requestMutualAuth(boolean state) throws GSSException;

    public void requestReplayDet(boolean state) throws GSSException;

    public void requestSequenceDet(boolean state) throws GSSException;

    public void requestCredDeleg(boolean state) throws GSSException;

    public void requestAnonymity(boolean state) throws GSSException;

    public void requestConf(boolean state) throws GSSException;

    public void requestInteg(boolean state) throws GSSException;

    public void requestLifetime(int lifetime) throws GSSException;

    public void setChannelBinding(ChannelBinding cb) throws GSSException;

    public boolean getCredDelegState();

    public boolean getMutualAuthState();

    public boolean getReplayDetState();

    public boolean getSequenceDetState();

    public boolean getAnonymityState();

    public boolean isTransferable() throws GSSException;

    public boolean isProtReady();

    public boolean getConfState();

    public boolean getIntegState();

    public int getLifetime();

    public GSSName getSrcName() throws GSSException;

    public GSSName getTargName() throws GSSException;

    public Oid getMech() throws GSSException;

    public GSSCredential getDelegCred() throws GSSException;

    public boolean isInitiator() throws GSSException;
}
