/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.management;

public interface CompilationMXBean extends PlatformManagedObject{
    public String getName();

    public boolean isCompilationTimeMonitoringSupported();

    public long getTotalCompilationTime();
}
