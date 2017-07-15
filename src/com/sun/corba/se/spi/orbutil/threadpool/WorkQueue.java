/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.orbutil.threadpool;

public interface WorkQueue{
    public void addWork(Work aWorkItem);

    public String getName();

    public long totalWorkItemsAdded();

    public int workItemsInQueue();

    public long averageTimeInQueue();

    public ThreadPool getThreadPool();

    public void setThreadPool(ThreadPool aThreadPool);
}
// End of file.
