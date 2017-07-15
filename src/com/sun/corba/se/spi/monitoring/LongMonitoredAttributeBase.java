/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.monitoring;

public abstract class LongMonitoredAttributeBase extends MonitoredAttributeBase{
    ///////////////////////////////////////
    // operations

    public LongMonitoredAttributeBase(String name,String description){
        super(name);
        MonitoredAttributeInfoFactory f=
                MonitoringFactories.getMonitoredAttributeInfoFactory();
        MonitoredAttributeInfo maInfo=f.createMonitoredAttributeInfo(
                description,Long.class,false,false);
        this.setMonitoredAttributeInfo(maInfo);
    } // end LongMonitoredAttributeBase
} // end LongMonitoredAttributeBase
