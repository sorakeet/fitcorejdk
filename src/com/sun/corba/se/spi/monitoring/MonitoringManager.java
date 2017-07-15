/**
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.monitoring;

import java.io.Closeable;

public interface MonitoringManager extends Closeable{
    ///////////////////////////////////////
    // operations

    public MonitoredObject getRootMonitoredObject();

    public void clearState();
} // end MonitoringManager
