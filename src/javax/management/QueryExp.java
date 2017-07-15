/**
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;
// java import

import java.io.Serializable;

public interface QueryExp extends Serializable{
    public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
            BadAttributeValueExpException, InvalidApplicationException;

    public void setMBeanServer(MBeanServer s);
}
