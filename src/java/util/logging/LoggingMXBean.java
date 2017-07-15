/**
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.logging;

public interface LoggingMXBean{
    public java.util.List<String> getLoggerNames();

    public String getLoggerLevel(String loggerName);

    public void setLoggerLevel(String loggerName,String levelName);

    public String getParentLoggerName(String loggerName);
}
