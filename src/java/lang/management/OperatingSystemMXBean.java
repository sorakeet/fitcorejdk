/**
 * Copyright (c) 2003, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.management;

public interface OperatingSystemMXBean extends PlatformManagedObject{
    public String getName();

    public String getArch();

    public String getVersion();

    public int getAvailableProcessors();

    public double getSystemLoadAverage();
}
