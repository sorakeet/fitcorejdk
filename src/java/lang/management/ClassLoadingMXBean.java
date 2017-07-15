/**
 * Copyright (c) 2003, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.management;

public interface ClassLoadingMXBean extends PlatformManagedObject{
    public long getTotalLoadedClassCount();

    public int getLoadedClassCount();

    public long getUnloadedClassCount();

    public boolean isVerbose();

    public void setVerbose(boolean value);
}
