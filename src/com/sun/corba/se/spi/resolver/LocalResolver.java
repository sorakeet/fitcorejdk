/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.resolver;

import com.sun.corba.se.spi.orbutil.closure.Closure;

public interface LocalResolver extends Resolver{
    void register(String name,Closure closure);
}
