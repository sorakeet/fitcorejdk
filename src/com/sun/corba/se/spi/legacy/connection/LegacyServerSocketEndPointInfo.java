/**
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.legacy.connection;

public interface LegacyServerSocketEndPointInfo{
    // NAME is used while we still have a "port-exchange" ORBD
    // to get what used to be called "default" or "bootstrap" endpoints.
    public static final String DEFAULT_ENDPOINT="DEFAULT_ENDPOINT";
    public static final String BOOT_NAMING="BOOT_NAMING";
    public static final String NO_NAME="NO_NAME";

    public String getType();

    public String getHostName();

    public int getPort();

    public int getLocatorPort();

    public void setLocatorPort(int port);

    public String getName();
}
// End of file.
