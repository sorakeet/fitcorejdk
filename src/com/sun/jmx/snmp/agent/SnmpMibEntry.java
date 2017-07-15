/**
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;
// java imports
//

import com.sun.jmx.snmp.SnmpDefinitions;
import com.sun.jmx.snmp.SnmpStatusException;

import java.io.Serializable;

public abstract class SnmpMibEntry extends SnmpMibNode
        implements Serializable{
    public long getNextVarId(long id,Object userData)
            throws SnmpStatusException{
        long nextvar=super.getNextVarId(id,userData);
        while(!isReadable(nextvar))
            nextvar=super.getNextVarId(nextvar,userData);
        return nextvar;
    }

    public abstract boolean isReadable(long arc);

    abstract public void get(SnmpMibSubRequest req,int depth)
            throws SnmpStatusException;

    abstract public void set(SnmpMibSubRequest req,int depth)
            throws SnmpStatusException;

    abstract public void check(SnmpMibSubRequest req,int depth)
            throws SnmpStatusException;

    public void validateVarId(long arc,Object userData)
            throws SnmpStatusException{
        if(isVariable(arc)==false){
            throw new SnmpStatusException(SnmpDefinitions.snmpRspNoSuchName);
        }
    }

    public abstract boolean isVariable(long arc);
}
