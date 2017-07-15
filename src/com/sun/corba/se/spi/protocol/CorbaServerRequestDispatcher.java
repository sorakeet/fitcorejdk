/**
 * Copyright (c) 1998, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.protocol;

import com.sun.corba.se.pept.protocol.ServerRequestDispatcher;
import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.ObjectKey;
// XXX These must all be replaced by Sun private APIs.

public interface CorbaServerRequestDispatcher
        extends ServerRequestDispatcher{
    public IOR locate(ObjectKey key);
}
// End of file.
