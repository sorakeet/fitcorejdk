/**
 * Copyright (c) 2003, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.remote;

import javax.management.MBeanServer;

public interface MBeanServerForwarder extends MBeanServer{
    public MBeanServer getMBeanServer();

    public void setMBeanServer(MBeanServer mbs);
}
