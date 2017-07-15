/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.remote.security;

import javax.management.Notification;
import javax.management.ObjectName;
import javax.security.auth.Subject;

public interface NotificationAccessController{
    public void addNotificationListener(String connectionId,
                                        ObjectName name,
                                        Subject subject)
            throws SecurityException;

    public void removeNotificationListener(String connectionId,
                                           ObjectName name,
                                           Subject subject)
            throws SecurityException;

    public void fetchNotification(String connectionId,
                                  ObjectName name,
                                  Notification notification,
                                  Subject subject)
            throws SecurityException;
}
