/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.ior;

import org.omg.CORBA.ORB;

public interface TaggedComponent extends Identifiable{
    org.omg.IOP.TaggedComponent getIOPComponent(ORB orb);
}
