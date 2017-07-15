/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.ior;

import java.util.Iterator;

public interface ObjectAdapterId extends Writeable{
    int getNumLevels();

    Iterator iterator();

    String[] getAdapterName();
}
