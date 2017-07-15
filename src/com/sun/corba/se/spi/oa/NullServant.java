/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.oa;

import org.omg.CORBA.SystemException;

public interface NullServant{
    SystemException getException();
}
