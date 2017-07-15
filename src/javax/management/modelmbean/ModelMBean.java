/**
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
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

public interface ModelMBean extends
        DynamicMBean,
        PersistentMBean,
        ModelMBeanNotificationBroadcaster{
    public void setModelMBeanInfo(ModelMBeanInfo inModelMBeanInfo)
            throws MBeanException, RuntimeOperationsException;

    public void setManagedResource(Object mr,String mr_type)
            throws MBeanException, RuntimeOperationsException,
            InstanceNotFoundException, InvalidTargetObjectTypeException;
}
