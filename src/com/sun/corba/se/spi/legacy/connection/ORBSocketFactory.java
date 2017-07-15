/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.legacy.connection;

import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.transport.SocketInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public interface ORBSocketFactory{
    public static final String IIOP_CLEAR_TEXT="IIOP_CLEAR_TEXT";

    public ServerSocket createServerSocket(String type,int port)
            throws
            IOException;

    public SocketInfo getEndPointInfo(org.omg.CORBA.ORB orb,
                                      IOR ior,
                                      SocketInfo socketInfo);

    public Socket createSocket(SocketInfo socketInfo)
            throws
            IOException,
            GetEndPointInfoAgainException;
}
// End of file.
