/**
 * Copyright (c) 2003, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.remote;

import javax.management.MBeanServer;
import java.io.IOException;
import java.util.Map;

public interface JMXConnectorServerProvider{
    public JMXConnectorServer newJMXConnectorServer(JMXServiceURL serviceURL,
                                                    Map<String,?> environment,
                                                    MBeanServer mbeanServer)
            throws IOException;
}
