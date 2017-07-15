/**
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.remote.internal;

import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.remote.TargetedNotification;
import java.util.List;

public interface NotificationBufferFilter{
    public void apply(List<TargetedNotification> targetedNotifs,
                      ObjectName source,Notification notif);
}
