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
package javax.management;

public interface PersistentMBean{
    public void load()
            throws MBeanException, RuntimeOperationsException, InstanceNotFoundException;

    public void store()
            throws MBeanException, RuntimeOperationsException, InstanceNotFoundException;
}
