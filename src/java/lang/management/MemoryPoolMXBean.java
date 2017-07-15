/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.management;

public interface MemoryPoolMXBean extends PlatformManagedObject{
    public String getName();

    public MemoryType getType();

    public MemoryUsage getUsage();

    public MemoryUsage getPeakUsage();

    public void resetPeakUsage();

    public boolean isValid();

    public String[] getMemoryManagerNames();

    public long getUsageThreshold();

    public void setUsageThreshold(long threshold);

    public boolean isUsageThresholdExceeded();

    public long getUsageThresholdCount();

    public boolean isUsageThresholdSupported();

    public long getCollectionUsageThreshold();

    public void setCollectionUsageThreshold(long threshold);

    public boolean isCollectionUsageThresholdExceeded();

    public long getCollectionUsageThresholdCount();

    public MemoryUsage getCollectionUsage();

    public boolean isCollectionUsageThresholdSupported();
}
