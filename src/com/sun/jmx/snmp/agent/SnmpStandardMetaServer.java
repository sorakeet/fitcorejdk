/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;
// jmx imports
//

import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.SnmpValue;

public interface SnmpStandardMetaServer{
    public SnmpValue get(long arc,Object userData)
            throws SnmpStatusException;

    public SnmpValue set(SnmpValue x,long arc,Object userData)
            throws SnmpStatusException;

    public void check(SnmpValue x,long arc,Object userData)
            throws SnmpStatusException;
}
