/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.orbutil.threadpool;

public interface Work{
    public void doWork();

    public long getEnqueueTime();

    public void setEnqueueTime(long timeInMillis);

    public String getName();
}
// End of file.
