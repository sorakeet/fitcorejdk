/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws.handler;

import javax.xml.namespace.QName;

public interface PortInfo{
    public QName getServiceName();

    public QName getPortName();

    public String getBindingID();
}
