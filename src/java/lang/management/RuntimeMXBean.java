/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.management;

public interface RuntimeMXBean extends PlatformManagedObject{
    public String getName();

    public String getVmName();

    public String getVmVendor();

    public String getVmVersion();

    public String getSpecName();

    public String getSpecVendor();

    public String getSpecVersion();

    public String getManagementSpecVersion();

    public String getClassPath();

    public String getLibraryPath();

    public boolean isBootClassPathSupported();

    public String getBootClassPath();

    public java.util.List<String> getInputArguments();

    public long getUptime();

    public long getStartTime();

    public java.util.Map<String,String> getSystemProperties();
}
