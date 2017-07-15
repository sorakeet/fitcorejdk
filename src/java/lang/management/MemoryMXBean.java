/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.management;

public interface MemoryMXBean extends PlatformManagedObject{
    public int getObjectPendingFinalizationCount();

    public MemoryUsage getHeapMemoryUsage();

    public MemoryUsage getNonHeapMemoryUsage();

    public boolean isVerbose();

    public void setVerbose(boolean value);

    public void gc();
}
