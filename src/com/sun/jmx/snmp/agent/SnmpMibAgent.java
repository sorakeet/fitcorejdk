/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;
// java imports
//

import com.sun.jmx.snmp.*;

import javax.management.*;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
// jmx imports
//

public abstract class SnmpMibAgent
        implements SnmpMibAgentMBean, MBeanRegistration, Serializable{
    // ---------------------------------------------------------------------
    // PROTECTED VARIABLES
    // ---------------------------------------------------------------------
    protected String mibName;
    // ---------------------------------------------------------------------
    // PUBLIC METHODS
    //----------------------------------------------------------------------
    protected MBeanServer server;
    // ---------------------------------------------------------------------
    // PRIVATE VARIABLES
    // ---------------------------------------------------------------------
    private ObjectName adaptorName;
    private transient SnmpMibHandler adaptor;

    public SnmpMibAgent(){
    }

    public static SnmpMibRequest newMibRequest(SnmpPdu reqPdu,
                                               Vector<SnmpVarBind> vblist,
                                               int version,
                                               Object userData){
        return new SnmpMibRequestImpl(null,
                reqPdu,
                vblist,
                version,
                userData,
                null,
                SnmpDefinitions.noAuthNoPriv,
                getSecurityModel(version),
                null,null);
    }

    private static int getSecurityModel(int version){
        switch(version){
            case SnmpDefinitions.snmpVersionOne:
                return SnmpDefinitions.snmpV1SecurityModel;
            default:
                return SnmpDefinitions.snmpV2SecurityModel;
        }
    }

    public static SnmpMibRequest newMibRequest(SnmpEngine engine,
                                               SnmpPdu reqPdu,
                                               Vector<SnmpVarBind> vblist,
                                               int version,
                                               Object userData,
                                               String principal,
                                               int securityLevel,
                                               int securityModel,
                                               byte[] contextName,
                                               byte[] accessContextName){
        return new SnmpMibRequestImpl(engine,
                reqPdu,
                vblist,
                version,
                userData,
                principal,
                securityLevel,
                securityModel,
                contextName,
                accessContextName);
    }

    public abstract void init() throws IllegalAccessException;

    @Override
    public abstract ObjectName preRegister(MBeanServer server,
                                           ObjectName name)
            throws Exception;

    @Override
    public void postRegister(Boolean registrationDone){
    }

    @Override
    public void preDeregister() throws Exception{
    }
    // ---------------------------------------------------------------------
    // GETTERS AND SETTERS
    // ---------------------------------------------------------------------

    @Override
    public void postDeregister(){
    }

    @Override
    public abstract void get(SnmpMibRequest req)
            throws SnmpStatusException;

    @Override
    public abstract void getNext(SnmpMibRequest req)
            throws SnmpStatusException;

    @Override
    public abstract void getBulk(SnmpMibRequest req,int nonRepeat,
                                 int maxRepeat)
            throws SnmpStatusException;

    @Override
    public abstract void set(SnmpMibRequest req)
            throws SnmpStatusException;

    @Override
    public abstract void check(SnmpMibRequest req)
            throws SnmpStatusException;

    @Override
    public MBeanServer getMBeanServer(){
        return server;
    }

    @Override
    public SnmpMibHandler getSnmpAdaptor(){
        return adaptor;
    }

    @Override
    public void setSnmpAdaptor(SnmpMibHandler stack){
        if(adaptor!=null){
            adaptor.removeMib(this);
        }
        adaptor=stack;
        if(adaptor!=null){
            adaptor.addMib(this);
        }
    }

    @Override
    public void setSnmpAdaptor(SnmpMibHandler stack,SnmpOid[] oids){
        if(adaptor!=null){
            adaptor.removeMib(this);
        }
        adaptor=stack;
        if(adaptor!=null){
            adaptor.addMib(this,oids);
        }
    }

    @Override
    public void setSnmpAdaptor(SnmpMibHandler stack,String contextName){
        if(adaptor!=null){
            adaptor.removeMib(this,contextName);
        }
        adaptor=stack;
        if(adaptor!=null){
            adaptor.addMib(this,contextName);
        }
    }

    @Override
    public void setSnmpAdaptor(SnmpMibHandler stack,
                               String contextName,
                               SnmpOid[] oids){
        if(adaptor!=null){
            adaptor.removeMib(this,contextName);
        }
        adaptor=stack;
        if(adaptor!=null){
            adaptor.addMib(this,contextName,oids);
        }
    }

    @Override
    public ObjectName getSnmpAdaptorName(){
        return adaptorName;
    }

    @Override
    public void setSnmpAdaptorName(ObjectName name)
            throws InstanceNotFoundException, ServiceNotFoundException{
        if(server==null){
            throw new ServiceNotFoundException(mibName+" is not registered in the MBean server");
        }
        // First remove the reference on the old adaptor server.
        //
        if(adaptor!=null){
            adaptor.removeMib(this);
        }
        // Then update the reference to the new adaptor server.
        //
        Object[] params={this};
        String[] signature={"com.sun.jmx.snmp.agent.SnmpMibAgent"};
        try{
            adaptor=(SnmpMibHandler)(server.invoke(name,"addMib",params,
                    signature));
        }catch(InstanceNotFoundException e){
            throw new InstanceNotFoundException(name.toString());
        }catch(ReflectionException e){
            throw new ServiceNotFoundException(name.toString());
        }catch(MBeanException e){
            // Should never occur...
        }
        adaptorName=name;
    }

    @Override
    public void setSnmpAdaptorName(ObjectName name,SnmpOid[] oids)
            throws InstanceNotFoundException, ServiceNotFoundException{
        if(server==null){
            throw new ServiceNotFoundException(mibName+" is not registered in the MBean server");
        }
        // First remove the reference on the old adaptor server.
        //
        if(adaptor!=null){
            adaptor.removeMib(this);
        }
        // Then update the reference to the new adaptor server.
        //
        Object[] params={this,oids};
        String[] signature={"com.sun.jmx.snmp.agent.SnmpMibAgent",
                oids.getClass().getName()};
        try{
            adaptor=(SnmpMibHandler)(server.invoke(name,"addMib",params,
                    signature));
        }catch(InstanceNotFoundException e){
            throw new InstanceNotFoundException(name.toString());
        }catch(ReflectionException e){
            throw new ServiceNotFoundException(name.toString());
        }catch(MBeanException e){
            // Should never occur...
        }
        adaptorName=name;
    }
    // ---------------------------------------------------------------------
    // PACKAGE METHODS
    // ---------------------------------------------------------------------

    @Override
    public void setSnmpAdaptorName(ObjectName name,String contextName)
            throws InstanceNotFoundException, ServiceNotFoundException{
        if(server==null){
            throw new ServiceNotFoundException(mibName+" is not registered in the MBean server");
        }
        // First remove the reference on the old adaptor server.
        //
        if(adaptor!=null){
            adaptor.removeMib(this,contextName);
        }
        // Then update the reference to the new adaptor server.
        //
        Object[] params={this,contextName};
        String[] signature={"com.sun.jmx.snmp.agent.SnmpMibAgent","java.lang.String"};
        try{
            adaptor=(SnmpMibHandler)(server.invoke(name,"addMib",params,
                    signature));
        }catch(InstanceNotFoundException e){
            throw new InstanceNotFoundException(name.toString());
        }catch(ReflectionException e){
            throw new ServiceNotFoundException(name.toString());
        }catch(MBeanException e){
            // Should never occur...
        }
        adaptorName=name;
    }
    // ---------------------------------------------------------------------
    // PRIVATE METHODS
    // ---------------------------------------------------------------------

    @Override
    public void setSnmpAdaptorName(ObjectName name,
                                   String contextName,SnmpOid[] oids)
            throws InstanceNotFoundException, ServiceNotFoundException{
        if(server==null){
            throw new ServiceNotFoundException(mibName+" is not registered in the MBean server");
        }
        // First remove the reference on the old adaptor server.
        //
        if(adaptor!=null){
            adaptor.removeMib(this,contextName);
        }
        // Then update the reference to the new adaptor server.
        //
        Object[] params={this,contextName,oids};
        String[] signature={"com.sun.jmx.snmp.agent.SnmpMibAgent","java.lang.String",oids.getClass().getName()};
        try{
            adaptor=(SnmpMibHandler)(server.invoke(name,"addMib",params,
                    signature));
        }catch(InstanceNotFoundException e){
            throw new InstanceNotFoundException(name.toString());
        }catch(ReflectionException e){
            throw new ServiceNotFoundException(name.toString());
        }catch(MBeanException e){
            // Should never occur...
        }
        adaptorName=name;
    }

    @Override
    public boolean getBindingState(){
        if(adaptor==null)
            return false;
        else
            return true;
    }

    @Override
    public String getMibName(){
        return mibName;
    }

    public abstract long[] getRootOid();

    void getBulkWithGetNext(SnmpMibRequest req,int nonRepeat,int maxRepeat)
            throws SnmpStatusException{
        final Vector<SnmpVarBind> list=req.getSubList();
        // RFC 1905, Section 4.2.3, p14
        final int L=list.size();
        final int N=Math.max(Math.min(nonRepeat,L),0);
        final int M=Math.max(maxRepeat,0);
        final int R=L-N;
        // Let's build the varBindList for the response pdu
        //
        // int errorStatus = SnmpDefinitions.snmpRspNoError ;
        // int errorIndex = 0 ;
        if(L!=0){
            // Non-repeaters and first row of repeaters
            //
            getNext(req);
            // Now the remaining repeaters
            //
            Vector<SnmpVarBind> repeaters=splitFrom(list,N);
            SnmpMibRequestImpl repeatedReq=
                    new SnmpMibRequestImpl(req.getEngine(),
                            req.getPdu(),
                            repeaters,
                            SnmpDefinitions.snmpVersionTwo,
                            req.getUserData(),
                            req.getPrincipal(),
                            req.getSecurityLevel(),
                            req.getSecurityModel(),
                            req.getContextName(),
                            req.getAccessContextName());
            for(int i=2;i<=M;i++){
                getNext(repeatedReq);
                concatVector(req,repeaters);
            }
        }
    }

    private Vector<SnmpVarBind> splitFrom(Vector<SnmpVarBind> original,int limit){
        int max=original.size();
        Vector<SnmpVarBind> result=new Vector<>(max-limit);
        int i=limit;
        // Ok the loop looks a bit strange. But in order to improve the
        // perf, we try to avoid reference to the limit variable from
        // within the loop ...
        //
        for(Enumeration<SnmpVarBind> e=original.elements();e.hasMoreElements();--i){
            SnmpVarBind var=e.nextElement();
            if(i>0)
                continue;
            result.addElement(new SnmpVarBind(var.oid,var.value));
        }
        return result;
    }

    private void concatVector(SnmpMibRequest req,Vector<SnmpVarBind> source){
        for(Enumeration<SnmpVarBind> e=source.elements();e.hasMoreElements();){
            SnmpVarBind var=e.nextElement();
            // We need to duplicate the SnmpVarBind otherwise it is going
            // to be overloaded by the next get Next ...
            req.addVarBind(new SnmpVarBind(var.oid,var.value));
        }
    }
}
