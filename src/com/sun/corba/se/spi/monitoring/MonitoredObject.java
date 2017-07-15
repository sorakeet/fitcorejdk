/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.monitoring;

import java.util.Collection;

public interface MonitoredObject{
    ///////////////////////////////////////
    // operations
    public String getName();

    public String getDescription();

    public void addChild(MonitoredObject m);

    public void removeChild(String name);

    public MonitoredObject getChild(String name);

    public Collection getChildren();

    public MonitoredObject getParent();

    public void setParent(MonitoredObject m);

    public void addAttribute(MonitoredAttribute value);

    public void removeAttribute(String name);

    public MonitoredAttribute getAttribute(String name);

    public Collection getAttributes();

    public void clearState();
} // end MonitoredObject
