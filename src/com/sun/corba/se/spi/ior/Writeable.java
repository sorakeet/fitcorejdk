/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.ior;

import org.omg.CORBA_2_3.portable.OutputStream;

public interface Writeable{
    void write(OutputStream arg0);
}
