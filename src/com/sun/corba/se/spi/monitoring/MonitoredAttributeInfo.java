/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.monitoring;

public interface MonitoredAttributeInfo{
    ///////////////////////////////////////
    // operations

    public boolean isWritable();

    public boolean isStatistic();

    public Class type();

    public String getDescription();
} // end MonitoredAttributeInfo
