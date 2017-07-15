/**
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.daemon;

public interface CommunicatorServerMBean{
    public void start();

    public void stop();

    public boolean isActive();

    public boolean waitState(int state,long timeOut);

    public int getState();

    public String getStateString();

    public String getHost();

    public int getPort();

    public void setPort(int port) throws IllegalStateException;

    public abstract String getProtocol();
}
