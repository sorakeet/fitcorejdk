/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.monitoring;

public interface MonitoredAttribute{
    ///////////////////////////////////////
    // operations

    public MonitoredAttributeInfo getAttributeInfo();

    public Object getValue();

    public void setValue(Object value);

    public String getName();

    public void clearState();
} // end MonitoredAttribute
