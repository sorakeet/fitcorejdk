/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.monitoring;

import com.sun.corba.se.impl.monitoring.MonitoredAttributeInfoFactoryImpl;
import com.sun.corba.se.impl.monitoring.MonitoredObjectFactoryImpl;
import com.sun.corba.se.impl.monitoring.MonitoringManagerFactoryImpl;

public class MonitoringFactories{
    ///////////////////////////////////////
    // attributes
    private static final MonitoredObjectFactoryImpl monitoredObjectFactory=
            new MonitoredObjectFactoryImpl();
    private static final MonitoredAttributeInfoFactoryImpl
            monitoredAttributeInfoFactory=
            new MonitoredAttributeInfoFactoryImpl();
    private static final MonitoringManagerFactoryImpl monitoringManagerFactory=
            new MonitoringManagerFactoryImpl();
    ///////////////////////////////////////
    // operations

    public static MonitoredObjectFactory getMonitoredObjectFactory(){
        return monitoredObjectFactory;
    }

    public static MonitoredAttributeInfoFactory
    getMonitoredAttributeInfoFactory(){
        return monitoredAttributeInfoFactory;
    }

    public static MonitoringManagerFactory getMonitoringManagerFactory(){
        return monitoringManagerFactory;
    }
}
