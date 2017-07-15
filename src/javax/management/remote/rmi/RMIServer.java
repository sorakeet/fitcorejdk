/**
 * Copyright (c) 2002, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.remote.rmi;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIServer extends Remote{
    public String getVersion() throws RemoteException;

    public RMIConnection newClient(Object credentials) throws IOException;
}
