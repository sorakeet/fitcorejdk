/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 */
/**
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 */
package javax.rmi.CORBA;

import org.omg.CORBA.ORB;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;

public interface StubDelegate{
    int hashCode(Stub self);

    boolean equals(Stub self,Object obj);

    String toString(Stub self);

    void connect(Stub self,ORB orb)
            throws RemoteException;

    // _REVISIT_ cannot link to Stub.readObject directly... why not?
    void readObject(Stub self,ObjectInputStream s)
            throws IOException, ClassNotFoundException;

    // _REVISIT_ cannot link to Stub.writeObject directly... why not?
    void writeObject(Stub self,ObjectOutputStream s)
            throws IOException;
}
