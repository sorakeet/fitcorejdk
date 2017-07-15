/**
 * Copyright (c) 2002, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public interface NotificationEmitter extends NotificationBroadcaster{
    public void removeNotificationListener(NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
            throws ListenerNotFoundException;
}
