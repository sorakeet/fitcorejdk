/**
 * Copyright (c) 2002, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import com.sun.jmx.mbeanserver.JmxMBeanServer;

public class MBeanServerBuilder{
    public MBeanServerBuilder(){
    }

    public MBeanServerDelegate newMBeanServerDelegate(){
        return JmxMBeanServer.newMBeanServerDelegate();
    }

    public MBeanServer newMBeanServer(String defaultDomain,
                                      MBeanServer outer,
                                      MBeanServerDelegate delegate){
        // By default, MBeanServerInterceptors are disabled.
        // Use com.sun.jmx.mbeanserver.MBeanServerBuilder to obtain
        // MBeanServers on which MBeanServerInterceptors are enabled.
        return JmxMBeanServer.newMBeanServer(defaultDomain,outer,delegate,
                false);
    }
}
