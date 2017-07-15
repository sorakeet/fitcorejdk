/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.management;

public interface ThreadMXBean extends PlatformManagedObject{
    public int getThreadCount();

    public int getPeakThreadCount();

    public long getTotalStartedThreadCount();

    public int getDaemonThreadCount();

    public long[] getAllThreadIds();

    public ThreadInfo getThreadInfo(long id);

    public ThreadInfo[] getThreadInfo(long[] ids);

    public ThreadInfo getThreadInfo(long id,int maxDepth);

    public ThreadInfo[] getThreadInfo(long[] ids,int maxDepth);

    public boolean isThreadContentionMonitoringSupported();

    public boolean isThreadContentionMonitoringEnabled();

    public void setThreadContentionMonitoringEnabled(boolean enable);

    public long getCurrentThreadCpuTime();

    public long getCurrentThreadUserTime();

    public long getThreadCpuTime(long id);

    public long getThreadUserTime(long id);

    public boolean isThreadCpuTimeSupported();

    public boolean isCurrentThreadCpuTimeSupported();

    public boolean isThreadCpuTimeEnabled();

    public void setThreadCpuTimeEnabled(boolean enable);

    public long[] findMonitorDeadlockedThreads();

    public void resetPeakThreadCount();

    public long[] findDeadlockedThreads();

    public boolean isObjectMonitorUsageSupported();

    public boolean isSynchronizerUsageSupported();

    public ThreadInfo[] getThreadInfo(long[] ids,boolean lockedMonitors,boolean lockedSynchronizers);

    public ThreadInfo[] dumpAllThreads(boolean lockedMonitors,boolean lockedSynchronizers);
}
