/**
 * Copyright (c) 1996, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.registry;

import java.rmi.*;

public interface Registry extends Remote{
    public static final int REGISTRY_PORT=1099;

    public Remote lookup(String name)
            throws RemoteException, NotBoundException, AccessException;

    public void bind(String name,Remote obj)
            throws RemoteException, AlreadyBoundException, AccessException;

    public void unbind(String name)
            throws RemoteException, NotBoundException, AccessException;

    public void rebind(String name,Remote obj)
            throws RemoteException, AccessException;

    public String[] list() throws RemoteException, AccessException;
}
