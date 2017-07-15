/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * @author IBM Corp.
 * <p>
 * Copyright IBM Corp. 1999-2000.  All rights reserved.
 */
/**
 * @author IBM Corp.
 *
 * Copyright IBM Corp. 1999-2000.  All rights reserved.
 */
package javax.management.modelmbean;

import javax.management.*;

public interface ModelMBeanNotificationBroadcaster extends NotificationBroadcaster{
    public void sendNotification(Notification ntfyObj)
            throws MBeanException, RuntimeOperationsException;

    public void sendNotification(String ntfyText)
            throws MBeanException, RuntimeOperationsException;

    public void sendAttributeChangeNotification(AttributeChangeNotification notification)
            throws MBeanException, RuntimeOperationsException;

    public void sendAttributeChangeNotification(Attribute oldValue,Attribute newValue)
            throws MBeanException, RuntimeOperationsException;

    public void addAttributeChangeNotificationListener(NotificationListener listener,
                                                       String attributeName,
                                                       Object handback)
            throws MBeanException, RuntimeOperationsException, IllegalArgumentException;

    public void removeAttributeChangeNotificationListener(NotificationListener listener,
                                                          String attributeName)
            throws MBeanException, RuntimeOperationsException, ListenerNotFoundException;
}
