/**
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;

import com.sun.jmx.snmp.SnmpEngine;
import com.sun.jmx.snmp.SnmpPdu;
import com.sun.jmx.snmp.SnmpVarBind;

import java.util.Enumeration;
import java.util.Vector;

final class SnmpMibRequestImpl implements SnmpMibRequest{
    // -------------------------------------------------------------------
    // Private variables
    // -------------------------------------------------------------------
    // Ideally these variables should be declared final but it makes
    // the jdk1.1.x compiler complain (seems to be a compiler bug, jdk1.2
    // is OK).
    private Vector<SnmpVarBind> varbinds;
    // -------------------------------------------------------------------
    // PUBLIC METHODS from SnmpMibRequest
    // -------------------------------------------------------------------
    private int version;
    private Object data;
    private SnmpPdu reqPdu=null;
    // Non final variable.
    private SnmpRequestTree tree=null;
    private SnmpEngine engine=null;
    private String principal=null;
    private int securityLevel=-1;
    private int securityModel=-1;
    private byte[] contextName=null;
    private byte[] accessContextName=null;

    public SnmpMibRequestImpl(SnmpEngine engine,
                              SnmpPdu reqPdu,
                              Vector<SnmpVarBind> vblist,
                              int protocolVersion,
                              Object userData,
                              String principal,
                              int securityLevel,
                              int securityModel,
                              byte[] contextName,
                              byte[] accessContextName){
        varbinds=vblist;
        version=protocolVersion;
        data=userData;
        this.reqPdu=reqPdu;
        this.engine=engine;
        this.principal=principal;
        this.securityLevel=securityLevel;
        this.securityModel=securityModel;
        this.contextName=contextName;
        this.accessContextName=accessContextName;
    }

    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    @Override
    public final Enumeration<SnmpVarBind> getElements(){
        return varbinds.elements();
    }

    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    @Override
    public final Vector<SnmpVarBind> getSubList(){
        return varbinds;
    }

    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    @Override
    public final int getVersion(){
        return version;
    }

    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    @Override
    public final int getRequestPduVersion(){
        return reqPdu.version;
    }
    // -------------------------------------------------------------------
    // PACKAGE METHODS
    // -------------------------------------------------------------------

    @Override
    public SnmpEngine getEngine(){
        return engine;
    }

    @Override
    public String getPrincipal(){
        return principal;
    }

    @Override
    public int getSecurityLevel(){
        return securityLevel;
    }

    @Override
    public int getSecurityModel(){
        return securityModel;
    }

    @Override
    public byte[] getContextName(){
        return contextName;
    }

    @Override
    public byte[] getAccessContextName(){
        return accessContextName;
    }

    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    @Override
    public final Object getUserData(){
        return data;
    }

    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    @Override
    public final int getVarIndex(SnmpVarBind varbind){
        return varbinds.indexOf(varbind);
    }

    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    @Override
    public void addVarBind(SnmpVarBind varbind){
        varbinds.addElement(varbind);
    }

    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    @Override
    public final int getSize(){
        if(varbinds==null) return 0;
        return varbinds.size();
    }

    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    @Override
    public final SnmpPdu getPdu(){
        return reqPdu;
    }

    // -------------------------------------------------------------------
    // Returns the SnmpRequestTree object built in the first operation
    // phase for two-phase SNMP requests (like SET).
    // -------------------------------------------------------------------
    final SnmpRequestTree getRequestTree(){
        return tree;
    }

    // -------------------------------------------------------------------
    // Allow to pass the request tree built during the check() phase
    // to the set() method. Note: the if the tree is `null', then the
    // set() method will rebuild a new tree identical to the tree built
    // in the check() method.
    //
    // Passing this tree in the SnmpMibRequestImpl object allows to
    // optimize the SET requests.
    //
    // -------------------------------------------------------------------
    final void setRequestTree(SnmpRequestTree tree){
        this.tree=tree;
    }

    // -------------------------------------------------------------------
    // Returns the underlying vector of SNMP varbinds (used for algorithm
    // optimization).
    // -------------------------------------------------------------------
    final Vector<SnmpVarBind> getVarbinds(){
        return varbinds;
    }
}
