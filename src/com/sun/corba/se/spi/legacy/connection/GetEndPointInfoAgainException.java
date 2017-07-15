/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.legacy.connection;

import com.sun.corba.se.spi.transport.SocketInfo;

public class GetEndPointInfoAgainException
        extends Exception{
    private SocketInfo socketInfo;

    public GetEndPointInfoAgainException(SocketInfo socketInfo){
        this.socketInfo=socketInfo;
    }

    public SocketInfo getEndPointInfo(){
        return socketInfo;
    }
}
