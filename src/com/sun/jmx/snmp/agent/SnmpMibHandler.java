/**
 * Copyright (c) 1998, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;
// java imports
//

import com.sun.jmx.snmp.SnmpOid;
// jmx imports
//

public interface SnmpMibHandler{
    public SnmpMibHandler addMib(SnmpMibAgent mib) throws IllegalArgumentException;

    public SnmpMibHandler addMib(SnmpMibAgent mib,SnmpOid[] oids) throws IllegalArgumentException;

    public SnmpMibHandler addMib(SnmpMibAgent mib,String contextName)
            throws IllegalArgumentException;

    public SnmpMibHandler addMib(SnmpMibAgent mib,String contextName,SnmpOid[] oids)
            throws IllegalArgumentException;

    public boolean removeMib(SnmpMibAgent mib);

    public boolean removeMib(SnmpMibAgent mib,SnmpOid[] oids);

    public boolean removeMib(SnmpMibAgent mib,String contextName);

    public boolean removeMib(SnmpMibAgent mib,String contextName,SnmpOid[] oids);
}
