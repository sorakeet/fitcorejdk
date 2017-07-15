/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.monitoring;

public abstract class StringMonitoredAttributeBase
        extends MonitoredAttributeBase{
    ///////////////////////////////////////
    // operations

    public StringMonitoredAttributeBase(String name,String description){
        super(name);
        MonitoredAttributeInfoFactory f=
                MonitoringFactories.getMonitoredAttributeInfoFactory();
        MonitoredAttributeInfo maInfo=f.createMonitoredAttributeInfo(
                description,String.class,false,false);
        this.setMonitoredAttributeInfo(maInfo);
    } // end StringMonitoredAttributeBase
} // end StringMonitoredAttributeBase
