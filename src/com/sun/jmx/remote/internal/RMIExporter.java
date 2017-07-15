/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.remote.internal;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

public interface RMIExporter{
    public static final String EXPORTER_ATTRIBUTE=
            "com.sun.jmx.remote.rmi.exporter";

    public Remote exportObject(Remote obj,
                               int port,
                               RMIClientSocketFactory csf,
                               RMIServerSocketFactory ssf)
            throws RemoteException;

    public boolean unexportObject(Remote obj,boolean force)
            throws NoSuchObjectException;
}
