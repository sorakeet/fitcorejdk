/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.orbutil.threadpool;

public interface ThreadPoolChooser{
    public ThreadPool getThreadPool();

    public ThreadPool getThreadPool(int id);

    public String[] getThreadPoolIds();
}
