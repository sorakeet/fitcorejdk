/**
 * Copyright (c) 1999, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public interface ValueExp extends java.io.Serializable{
    public ValueExp apply(ObjectName name)
            throws BadStringOperationException, BadBinaryOpValueExpException,
            BadAttributeValueExpException, InvalidApplicationException;

    @Deprecated
    public void setMBeanServer(MBeanServer s);
}
