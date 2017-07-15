/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.ior;

public interface TaggedProfile extends Identifiable, MakeImmutable{
    TaggedProfileTemplate getTaggedProfileTemplate();

    ObjectId getObjectId();

    ObjectKeyTemplate getObjectKeyTemplate();

    ObjectKey getObjectKey();

    boolean isEquivalent(TaggedProfile prof);

    org.omg.IOP.TaggedProfile getIOPProfile();

    boolean isLocal();
}
