/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.oa;

import com.sun.corba.se.spi.ior.ObjectAdapterId;
import com.sun.corba.se.spi.orb.ORB;

public interface ObjectAdapterFactory{
    void init(ORB orb);

    void shutdown(boolean waitForCompletion);

    ObjectAdapter find(ObjectAdapterId oaid);

    ORB getORB();
}
