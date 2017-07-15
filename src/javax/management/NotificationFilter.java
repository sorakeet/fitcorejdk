/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public interface NotificationFilter extends java.io.Serializable{
    public boolean isNotificationEnabled(Notification notification);
}
