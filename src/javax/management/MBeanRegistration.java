/**
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

public interface MBeanRegistration{
    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name) throws Exception;

    public void postRegister(Boolean registrationDone);

    public void preDeregister() throws Exception;

    public void postDeregister();
}
