/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.ior;

import java.util.Iterator;
import java.util.List;

public interface IORTemplate extends List, IORFactory, MakeImmutable{
    Iterator iteratorById(int id);

    ObjectKeyTemplate getObjectKeyTemplate();
}
