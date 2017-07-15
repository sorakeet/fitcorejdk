/**
 * Copyright (c) 2003, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.remote.internal;

import javax.management.remote.NotificationResult;

public interface NotificationBuffer{
    public NotificationResult
    fetchNotifications(NotificationBufferFilter filter,
                       long startSequenceNumber,
                       long timeout,
                       int maxNotifications)
            throws InterruptedException;

    public void dispose();
}
