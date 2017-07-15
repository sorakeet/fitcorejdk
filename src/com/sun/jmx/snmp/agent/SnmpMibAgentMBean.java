/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;
// java imports
//

import com.sun.jmx.snmp.SnmpOid;
import com.sun.jmx.snmp.SnmpStatusException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ServiceNotFoundException;
// jmx imports
//

public interface SnmpMibAgentMBean{
    // PUBLIC METHODS
    //---------------

    public void get(SnmpMibRequest req) throws SnmpStatusException;

    public void getNext(SnmpMibRequest req) throws SnmpStatusException;

    public void getBulk(SnmpMibRequest req,int nonRepeat,int maxRepeat)
            throws SnmpStatusException;

    public void set(SnmpMibRequest req) throws SnmpStatusException;

    public void check(SnmpMibRequest req) throws SnmpStatusException;
    // GETTERS AND SETTERS
    //--------------------

    public MBeanServer getMBeanServer();

    public SnmpMibHandler getSnmpAdaptor();

    public void setSnmpAdaptor(SnmpMibHandler stack);

    public void setSnmpAdaptor(SnmpMibHandler stack,SnmpOid[] oids);

    public void setSnmpAdaptor(SnmpMibHandler stack,String contextName);

    public void setSnmpAdaptor(SnmpMibHandler stack,
                               String contextName,
                               SnmpOid[] oids);

    public ObjectName getSnmpAdaptorName();

    public void setSnmpAdaptorName(ObjectName name)
            throws InstanceNotFoundException, ServiceNotFoundException;

    public void setSnmpAdaptorName(ObjectName name,SnmpOid[] oids)
            throws InstanceNotFoundException, ServiceNotFoundException;

    public void setSnmpAdaptorName(ObjectName name,String contextName)
            throws InstanceNotFoundException, ServiceNotFoundException;

    public void setSnmpAdaptorName(ObjectName name,
                                   String contextName,
                                   SnmpOid[] oids)
            throws InstanceNotFoundException, ServiceNotFoundException;

    public boolean getBindingState();

    public String getMibName();
}
