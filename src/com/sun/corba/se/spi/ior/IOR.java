/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.ior;

import com.sun.corba.se.spi.ior.iiop.IIOPProfile;
import com.sun.corba.se.spi.orb.ORB;

import java.util.Iterator;
import java.util.List;

public interface IOR extends List, Writeable, MakeImmutable{
    ORB getORB();

    String getTypeId();

    Iterator iteratorById(int id);

    String stringify();

    org.omg.IOP.IOR getIOPIOR();

    boolean isNil();

    boolean isEquivalent(IOR ior);

    IORTemplateList getIORTemplates();

    IIOPProfile getProfile();
}
