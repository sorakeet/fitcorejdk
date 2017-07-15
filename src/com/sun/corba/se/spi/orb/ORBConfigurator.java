/**
 * Copyright (c) 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.orb;

public interface ORBConfigurator{
    void configure(DataCollector dc,ORB orb);
}
