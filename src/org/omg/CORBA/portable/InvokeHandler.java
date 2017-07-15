/**
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA.portable;

public interface InvokeHandler{
    OutputStream _invoke(String method,InputStream input,
                         ResponseHandler handler)
            throws org.omg.CORBA.SystemException;
}
