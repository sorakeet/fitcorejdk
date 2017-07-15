/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Copyright (c) 1995-96 by Cisco Systems, Inc.
package com.sun.jmx.snmp;
// java import
//

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Vector;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.SNMP_LOGGER;
//RI import

public class SnmpOidTableSupport implements SnmpOidTable{
    private Hashtable<String,SnmpOidRecord> oidStore=new Hashtable<>();
    private String myName;

    public SnmpOidTableSupport(String name){
        myName=name;
    }

    @Override
    public SnmpOidRecord resolveVarName(String name) throws SnmpStatusException{
        SnmpOidRecord var=oidStore.get(name);
        if(var!=null){
            return var;
        }else{
            throw new SnmpStatusException("Variable name <"+name+"> not found in Oid repository");
        }
    }

    @Override
    public SnmpOidRecord resolveVarOid(String oid) throws SnmpStatusException{
        // Try to see if the variable name is actually an OID to resolve.
        //
        int index=oid.indexOf('.');
        if(index<0){
            throw new SnmpStatusException("Variable oid <"+oid+"> not found in Oid repository");
        }
        if(index==0){
            // The oid starts with a '.' ala CMU.
            //
            oid=oid.substring(1,oid.length());
        }
        // Go through the oidStore ... Good luck !
        //
        for(Enumeration<SnmpOidRecord> list=oidStore.elements();list.hasMoreElements();){
            SnmpOidRecord element=list.nextElement();
            if(element.getOid().equals(oid))
                return element;
        }
        throw new SnmpStatusException("Variable oid <"+oid+"> not found in Oid repository");
    }

    @Override
    public Vector<SnmpOidRecord> getAllEntries(){
        Vector<SnmpOidRecord> elementsVector=new Vector<>();
        // get the locally defined elements ...
        for(Enumeration<SnmpOidRecord> e=oidStore.elements();
            e.hasMoreElements();){
            elementsVector.addElement(e.nextElement());
        }
        return elementsVector;
    }

    public synchronized void loadMib(SnmpOidRecord[] mibs){
        try{
            for(int i=0;;i++){
                SnmpOidRecord s=mibs[i];
                if(SNMP_LOGGER.isLoggable(Level.FINER)){
                    SNMP_LOGGER.logp(Level.FINER,
                            SnmpOidTableSupport.class.getName(),
                            "loadMib","Load "+s.getName());
                }
                oidStore.put(s.getName(),s);
            }
        }catch(ArrayIndexOutOfBoundsException e){
        }
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(myName);
    }

    @Override
    public boolean equals(Object object){
        if(!(object instanceof SnmpOidTableSupport)){
            return false;
        }
        SnmpOidTableSupport val=(SnmpOidTableSupport)object;
        return myName.equals(val.getName());
    }

    public String getName(){
        return myName;
    }
}
