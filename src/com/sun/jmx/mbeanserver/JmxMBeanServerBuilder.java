/**
 * Copyright (c) 2002, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.mbeanserver;

import javax.management.MBeanServer;
import javax.management.MBeanServerBuilder;
import javax.management.MBeanServerDelegate;

public class JmxMBeanServerBuilder extends MBeanServerBuilder{
    public MBeanServerDelegate newMBeanServerDelegate(){
        return JmxMBeanServer.newMBeanServerDelegate();
    }

    public MBeanServer newMBeanServer(String defaultDomain,
                                      MBeanServer outer,
                                      MBeanServerDelegate delegate){
        return JmxMBeanServer.newMBeanServer(defaultDomain,outer,delegate,
                true);
    }
}
