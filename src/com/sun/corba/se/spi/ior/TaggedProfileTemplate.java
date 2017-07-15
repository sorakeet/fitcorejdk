/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.ior;

import com.sun.corba.se.spi.orb.ORB;
import org.omg.CORBA_2_3.portable.OutputStream;

import java.util.Iterator;
import java.util.List;

public interface TaggedProfileTemplate extends List, Identifiable,
        WriteContents, MakeImmutable{
    public Iterator iteratorById(int id);

    TaggedProfile create(ObjectKeyTemplate oktemp,ObjectId id);

    void write(ObjectKeyTemplate oktemp,ObjectId id,OutputStream os);

    boolean isEquivalent(TaggedProfileTemplate temp);

    org.omg.IOP.TaggedComponent[] getIOPComponents(
            ORB orb,int id);
}
