/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.ior;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.protocol.CorbaServerRequestDispatcher;

public interface ObjectKey extends Writeable{
    ObjectId getId();

    ObjectKeyTemplate getTemplate();

    byte[] getBytes(org.omg.CORBA.ORB orb);

    CorbaServerRequestDispatcher getServerRequestDispatcher(ORB orb);
}
