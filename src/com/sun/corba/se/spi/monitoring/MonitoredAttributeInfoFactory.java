/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.monitoring;

public interface MonitoredAttributeInfoFactory{
    MonitoredAttributeInfo createMonitoredAttributeInfo(String description,
                                                        Class type,boolean isWritable,boolean isStatistic);
}
