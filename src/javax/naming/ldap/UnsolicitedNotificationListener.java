/**
 * Copyright (c) 1999, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

import javax.naming.event.NamingListener;

public interface UnsolicitedNotificationListener extends NamingListener{
    void notificationReceived(UnsolicitedNotificationEvent evt);
}
