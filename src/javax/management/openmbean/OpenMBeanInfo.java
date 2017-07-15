/**
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;
// java import
//
// jmx import
//

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;

public interface OpenMBeanInfo{
    // Re-declares the methods that are in class MBeanInfo of JMX 1.0
    // (methods will be removed when MBeanInfo is made a parent interface of this interface)

    public String getClassName();

    public String getDescription();

    public MBeanAttributeInfo[] getAttributes();

    public MBeanOperationInfo[] getOperations();

    public MBeanConstructorInfo[] getConstructors();

    public MBeanNotificationInfo[] getNotifications();
    // commodity methods
    //

    public int hashCode();

    public boolean equals(Object obj);

    public String toString();
}
