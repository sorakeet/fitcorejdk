/**
 * Copyright (c) 1998, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

import java.io.IOException;
import java.net.ServerSocket;

public interface RMIServerSocketFactory{
    public ServerSocket createServerSocket(int port)
            throws IOException;
}
