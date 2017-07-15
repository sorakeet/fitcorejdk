/**
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.monitor;
// jmx imports
//

import javax.management.ObjectName;

public interface CounterMonitorMBean extends MonitorMBean{
    // GETTERS AND SETTERS
    //--------------------

    @Deprecated
    public Number getDerivedGauge();

    @Deprecated
    public long getDerivedGaugeTimeStamp();

    @Deprecated
    public Number getThreshold();

    @Deprecated
    public void setThreshold(Number value) throws IllegalArgumentException;

    public Number getDerivedGauge(ObjectName object);

    public long getDerivedGaugeTimeStamp(ObjectName object);

    public Number getThreshold(ObjectName object);

    public Number getInitThreshold();

    public void setInitThreshold(Number value) throws IllegalArgumentException;

    public Number getOffset();

    public void setOffset(Number value) throws IllegalArgumentException;

    public Number getModulus();

    public void setModulus(Number value) throws IllegalArgumentException;

    public boolean getNotify();

    public void setNotify(boolean value);

    public boolean getDifferenceMode();

    public void setDifferenceMode(boolean value);
}
