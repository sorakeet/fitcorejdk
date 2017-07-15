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
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UtilDelegate{
    RemoteException mapSystemException(SystemException ex);

    void writeAny(OutputStream out,Object obj);

    Object readAny(InputStream in);

    void writeRemoteObject(OutputStream out,Object obj);

    void writeAbstractObject(OutputStream out,Object obj);

    void registerTarget(Tie tie,Remote target);

    void unexportObject(Remote target) throws java.rmi.NoSuchObjectException;

    Tie getTie(Remote target);

    ValueHandler createValueHandler();

    String getCodebase(Class clz);

    Class loadClass(String className,String remoteCodebase,ClassLoader loader)
            throws ClassNotFoundException;

    boolean isLocal(Stub stub) throws RemoteException;

    RemoteException wrapException(Throwable obj);

    Object copyObject(Object obj,ORB orb) throws RemoteException;

    Object[] copyObjects(Object[] obj,ORB orb) throws RemoteException;
}
