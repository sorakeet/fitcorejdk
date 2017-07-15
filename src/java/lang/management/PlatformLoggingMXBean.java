/**
 * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.management;

public interface PlatformLoggingMXBean extends PlatformManagedObject{
    java.util.List<String> getLoggerNames();

    String getLoggerLevel(String loggerName);

    void setLoggerLevel(String loggerName,String levelName);

    String getParentLoggerName(String loggerName);
}
