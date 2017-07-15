/**
 * Copyright (c) 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.resolver;

public interface Resolver{
    org.omg.CORBA.Object resolve(String name);

    java.util.Set list();
}
