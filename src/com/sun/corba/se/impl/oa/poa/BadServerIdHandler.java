/**
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.oa.poa;

import com.sun.corba.se.spi.ior.ObjectKey;

public interface BadServerIdHandler{
    void handle(ObjectKey objectKey);
}
