/**
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
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

public interface ValueHandler{
    void writeValue(org.omg.CORBA.portable.OutputStream out,
                    java.io.Serializable value);

    java.io.Serializable readValue(org.omg.CORBA.portable.InputStream in,
                                   int offset,
                                   Class clz,
                                   String repositoryID,
                                   org.omg.SendingContext.RunTime sender);

    String getRMIRepositoryID(Class clz);

    boolean isCustomMarshaled(Class clz);

    org.omg.SendingContext.RunTime getRunTimeCodeBase();

    java.io.Serializable writeReplace(java.io.Serializable value);
}
