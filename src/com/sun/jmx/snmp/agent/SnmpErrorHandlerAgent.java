/**
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;

import com.sun.jmx.snmp.SnmpDefinitions;
import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.SnmpVarBind;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.SNMP_ADAPTOR_LOGGER;

public class SnmpErrorHandlerAgent extends SnmpMibAgent
        implements Serializable{
    private static final long serialVersionUID=7751082923508885650L;

    public SnmpErrorHandlerAgent(){
    }

    @Override
    public void init() throws IllegalAccessException{
    }

    @Override
    public ObjectName preRegister(MBeanServer server,ObjectName name)
            throws Exception{
        return name;
    }

    @Override
    public void get(SnmpMibRequest inRequest) throws SnmpStatusException{
        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                SnmpErrorHandlerAgent.class.getName(),
                "get","Get in Exception");
        if(inRequest.getVersion()==SnmpDefinitions.snmpVersionOne)
            throw new SnmpStatusException(SnmpStatusException.noSuchName);
        Enumeration<SnmpVarBind> l=inRequest.getElements();
        while(l.hasMoreElements()){
            SnmpVarBind varbind=l.nextElement();
            varbind.setNoSuchObject();
        }
    }

    @Override
    public void getNext(SnmpMibRequest inRequest) throws SnmpStatusException{
        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                SnmpErrorHandlerAgent.class.getName(),
                "getNext","GetNext in Exception");
        if(inRequest.getVersion()==SnmpDefinitions.snmpVersionOne)
            throw new SnmpStatusException(SnmpStatusException.noSuchName);
        Enumeration<SnmpVarBind> l=inRequest.getElements();
        while(l.hasMoreElements()){
            SnmpVarBind varbind=l.nextElement();
            varbind.setEndOfMibView();
        }
    }

    @Override
    public void getBulk(SnmpMibRequest inRequest,int nonRepeat,int maxRepeat)
            throws SnmpStatusException{
        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                SnmpErrorHandlerAgent.class.getName(),
                "getBulk","GetBulk in Exception");
        if(inRequest.getVersion()==SnmpDefinitions.snmpVersionOne)
            throw new SnmpStatusException(SnmpDefinitions.snmpRspGenErr,0);
        Enumeration<SnmpVarBind> l=inRequest.getElements();
        while(l.hasMoreElements()){
            SnmpVarBind varbind=l.nextElement();
            varbind.setEndOfMibView();
        }
    }

    @Override
    public void set(SnmpMibRequest inRequest) throws SnmpStatusException{
        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                SnmpErrorHandlerAgent.class.getName(),
                "set","Set in Exception, CANNOT be called");
        throw new SnmpStatusException(SnmpDefinitions.snmpRspNotWritable);
    }

    @Override
    public void check(SnmpMibRequest inRequest) throws SnmpStatusException{
        SNMP_ADAPTOR_LOGGER.logp(Level.FINEST,
                SnmpErrorHandlerAgent.class.getName(),
                "check","Check in Exception");
        throw new SnmpStatusException(SnmpDefinitions.snmpRspNotWritable);
    }

    @Override
    public long[] getRootOid(){
        return null;
    }
}
