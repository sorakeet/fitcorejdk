/**
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public interface NotificationListener extends java.util.EventListener{
    public void handleNotification(Notification notification,Object handback);
}
