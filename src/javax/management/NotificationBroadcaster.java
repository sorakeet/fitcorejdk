/**
 * Copyright (c) 1999, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public interface NotificationBroadcaster{
    public void addNotificationListener(NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback)
            throws IllegalArgumentException;

    public void removeNotificationListener(NotificationListener listener)
            throws ListenerNotFoundException;

    public MBeanNotificationInfo[] getNotificationInfo();
}
