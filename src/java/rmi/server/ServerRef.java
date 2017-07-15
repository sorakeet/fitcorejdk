/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

@Deprecated
public interface ServerRef extends RemoteRef{
    static final long serialVersionUID=-4557750989390278438L;

    RemoteStub exportObject(Remote obj,Object data)
            throws RemoteException;

    String getClientHost() throws ServerNotActiveException;
}
