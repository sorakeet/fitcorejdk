/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.remote;

import java.io.IOException;
import java.util.Map;

public interface JMXConnectorProvider{
    public JMXConnector newJMXConnector(JMXServiceURL serviceURL,
                                        Map<String,?> environment)
            throws IOException;
}
