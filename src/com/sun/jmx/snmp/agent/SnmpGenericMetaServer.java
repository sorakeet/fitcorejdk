/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;
// jmx imports
//

import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.SnmpValue;

public interface SnmpGenericMetaServer{
    Object buildAttributeValue(long id,SnmpValue value)
            throws SnmpStatusException;

    SnmpValue buildSnmpValue(long id,Object value)
            throws SnmpStatusException;

    String getAttributeName(long id)
            throws SnmpStatusException;

    void checkSetAccess(SnmpValue x,long id,Object data)
            throws SnmpStatusException;

    void checkGetAccess(long id,Object data)
            throws SnmpStatusException;
}
