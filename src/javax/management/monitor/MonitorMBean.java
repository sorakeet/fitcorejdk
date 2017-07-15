/**
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.monitor;
// jmx imports
//

import javax.management.ObjectName;

public interface MonitorMBean{
    public void start();

    public void stop();
    // GETTERS AND SETTERS
    //--------------------

    public void addObservedObject(ObjectName object) throws IllegalArgumentException;

    public void removeObservedObject(ObjectName object);

    public boolean containsObservedObject(ObjectName object);

    public ObjectName[] getObservedObjects();

    @Deprecated
    public ObjectName getObservedObject();

    @Deprecated
    public void setObservedObject(ObjectName object);

    public String getObservedAttribute();

    public void setObservedAttribute(String attribute);

    public long getGranularityPeriod();

    public void setGranularityPeriod(long period) throws IllegalArgumentException;

    public boolean isActive();
}
