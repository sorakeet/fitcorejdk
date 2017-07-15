/**
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.monitoring;

public interface MonitoringManagerFactory{
    MonitoringManager createMonitoringManager(String nameOfTheRoot,
                                              String description);

    void remove(String nameOfTheRoot);
}
