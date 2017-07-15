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

public interface ModelMBeanInfo{
    public Descriptor[] getDescriptors(String inDescriptorType)
            throws MBeanException, RuntimeOperationsException;

    public void setDescriptors(Descriptor[] inDescriptors)
            throws MBeanException, RuntimeOperationsException;

    public Descriptor getDescriptor(String inDescriptorName,String inDescriptorType)
            throws MBeanException, RuntimeOperationsException;

    public void setDescriptor(Descriptor inDescriptor,String inDescriptorType)
            throws MBeanException, RuntimeOperationsException;

    public Descriptor getMBeanDescriptor()
            throws MBeanException, RuntimeOperationsException;

    public void setMBeanDescriptor(Descriptor inDescriptor)
            throws MBeanException, RuntimeOperationsException;

    public ModelMBeanAttributeInfo getAttribute(String inName)
            throws MBeanException, RuntimeOperationsException;

    public ModelMBeanOperationInfo getOperation(String inName)
            throws MBeanException, RuntimeOperationsException;

    public ModelMBeanNotificationInfo getNotification(String inName)
            throws MBeanException, RuntimeOperationsException;

    public Object clone();

    public MBeanAttributeInfo[] getAttributes();

    public String getClassName();

    public MBeanConstructorInfo[] getConstructors();

    public String getDescription();

    public MBeanNotificationInfo[] getNotifications();

    public MBeanOperationInfo[] getOperations();
}
