/**
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.monitor;
// jmx imports
//

import javax.management.ObjectName;

public interface StringMonitorMBean extends MonitorMBean{
    // GETTERS AND SETTERS
    //--------------------

    @Deprecated
    public String getDerivedGauge();

    @Deprecated
    public long getDerivedGaugeTimeStamp();

    public String getDerivedGauge(ObjectName object);

    public long getDerivedGaugeTimeStamp(ObjectName object);

    public String getStringToCompare();

    public void setStringToCompare(String value) throws IllegalArgumentException;

    public boolean getNotifyMatch();

    public void setNotifyMatch(boolean value);

    public boolean getNotifyDiffer();

    public void setNotifyDiffer(boolean value);
}
