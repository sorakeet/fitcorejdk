/**
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.presentation.rmi;

import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.Delegate;
import org.omg.CORBA.portable.OutputStream;

import java.rmi.RemoteException;

public interface DynamicStub extends org.omg.CORBA.Object{
    Delegate getDelegate();

    void setDelegate(Delegate delegate);

    ORB getORB();

    String[] getTypeIds();

    void connect(ORB orb) throws RemoteException;

    boolean isLocal();

    OutputStream request(String operation,boolean responseExpected);
}
