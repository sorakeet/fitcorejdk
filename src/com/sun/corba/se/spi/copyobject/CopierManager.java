/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.copyobject;

public interface CopierManager{
    int getDefaultId();

    void setDefaultId(int id);

    ObjectCopierFactory getObjectCopierFactory(int id);

    ObjectCopierFactory getDefaultObjectCopierFactory();

    void registerObjectCopierFactory(ObjectCopierFactory factory,int id);
}
