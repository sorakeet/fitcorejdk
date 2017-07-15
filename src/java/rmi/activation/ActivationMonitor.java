/**
 * Copyright (c) 1997, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.activation;

import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ActivationMonitor extends Remote{
    public void inactiveObject(ActivationID id)
            throws UnknownObjectException, RemoteException;

    public void activeObject(ActivationID id,
                             MarshalledObject<? extends Remote> obj)
            throws UnknownObjectException, RemoteException;

    public void inactiveGroup(ActivationGroupID id,
                              long incarnation)
            throws UnknownGroupException, RemoteException;
}
