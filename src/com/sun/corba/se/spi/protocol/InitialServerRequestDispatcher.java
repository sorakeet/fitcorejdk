/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.protocol;

import com.sun.corba.se.spi.resolver.Resolver;

public interface InitialServerRequestDispatcher
        extends CorbaServerRequestDispatcher{
    void init(Resolver resolver);
}
