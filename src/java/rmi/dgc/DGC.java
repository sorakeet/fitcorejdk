/**
 * Copyright (c) 1996, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.dgc;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ObjID;

public interface DGC extends Remote{
    Lease dirty(ObjID[] ids,long sequenceNum,Lease lease)
            throws RemoteException;

    void clean(ObjID[] ids,long sequenceNum,VMID vmid,boolean strong)
            throws RemoteException;
}
