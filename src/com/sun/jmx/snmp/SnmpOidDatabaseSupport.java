/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Copyright (c) 1995-96 by Cisco Systems, Inc.
package com.sun.jmx.snmp;
// java import
//

import java.util.Vector;

import static com.sun.jmx.mbeanserver.Util.cast;
// jmx import
//

public class SnmpOidDatabaseSupport implements SnmpOidDatabase{
    private Vector<SnmpOidTable> tables;

    public SnmpOidDatabaseSupport(){
        tables=new Vector<SnmpOidTable>();
    }

    public SnmpOidDatabaseSupport(SnmpOidTable table){
        tables=new Vector<SnmpOidTable>();
        tables.addElement(table);
    }

    public void add(SnmpOidTable table){
        if(!tables.contains(table)){
            tables.addElement(table);
        }
    }

    public void remove(SnmpOidTable table) throws SnmpStatusException{
        if(!tables.contains(table)){
            throw new SnmpStatusException("The specified SnmpOidTable does not exist in this SnmpOidDatabase");
        }
        tables.removeElement(table);
    }

    public void removeAll(){
        tables.removeAllElements();
    }

    public SnmpOidRecord resolveVarName(String name) throws SnmpStatusException{
        for(int i=0;i<tables.size();i++){
            try{
                return (tables.elementAt(i).resolveVarName(name));
            }catch(SnmpStatusException e){
                if(i==tables.size()-1){
                    throw new SnmpStatusException(e.getMessage());
                }
            }
        }
        return null;
    }

    public SnmpOidRecord resolveVarOid(String oid) throws SnmpStatusException{
        for(int i=0;i<tables.size();i++){
            try{
                return tables.elementAt(i).resolveVarOid(oid);
            }catch(SnmpStatusException e){
                if(i==tables.size()-1){
                    throw new SnmpStatusException(e.getMessage());
                }
            }
        }
        return null;
    }

    public Vector<?> getAllEntries(){
        Vector<SnmpOidTable> res=new Vector<SnmpOidTable>();
        for(int i=0;i<tables.size();i++){
            Vector<SnmpOidTable> tmp=cast(tables.elementAt(i).getAllEntries());
            if(tmp!=null){
                for(int ii=0;ii<tmp.size();ii++){
                    res.addElement(tmp.elementAt(ii));
                }
            }
        }
//      res.addAll(((SnmpOidTable)tables.elementAt(i)).getAllEntries());
        return res;
    }
}
