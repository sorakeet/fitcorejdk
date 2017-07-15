/**
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.monitor;
// jmx imports
//

import javax.management.ObjectName;

public interface GaugeMonitorMBean extends MonitorMBean{
    // GETTERS AND SETTERS
    //--------------------

    @Deprecated
    public Number getDerivedGauge();

    @Deprecated
    public long getDerivedGaugeTimeStamp();

    public Number getDerivedGauge(ObjectName object);

    public long getDerivedGaugeTimeStamp(ObjectName object);

    public Number getHighThreshold();

    public Number getLowThreshold();

    public void setThresholds(Number highValue,Number lowValue) throws IllegalArgumentException;

    public boolean getNotifyHigh();

    public void setNotifyHigh(boolean value);

    public boolean getNotifyLow();

    public void setNotifyLow(boolean value);

    public boolean getDifferenceMode();

    public void setDifferenceMode(boolean value);
}
