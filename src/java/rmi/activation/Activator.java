/**
 * Copyright (c) 1997, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.activation;

import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Activator extends Remote{
    public MarshalledObject<? extends Remote> activate(ActivationID id,
                                                       boolean force)
            throws ActivationException, UnknownObjectException, RemoteException;
}
