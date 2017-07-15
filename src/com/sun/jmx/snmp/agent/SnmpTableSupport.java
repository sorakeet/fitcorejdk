/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;
// java imports
//

import com.sun.jmx.snmp.SnmpOid;
import com.sun.jmx.snmp.SnmpStatusException;

import javax.management.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
// jmx imports
//

public abstract class SnmpTableSupport implements SnmpTableEntryFactory,
// NPCTE fix for bugId 4499265, esc 0, MR 04 sept 2001
//  SnmpTableCallbackHandler {
        SnmpTableCallbackHandler, Serializable{
// end of NPCTE fix for bugId 4499265
    //-----------------------------------------------------------------
    //
    //  Protected Variables
    //
    //-----------------------------------------------------------------
    protected List<Object> entries;
    protected SnmpMibTable meta;
    protected SnmpMib theMib;
    //-----------------------------------------------------------------
    //
    //  Private Variables
    //
    //-----------------------------------------------------------------
    private boolean registrationRequired=false;
    //-----------------------------------------------------------------
    //
    //  Constructor
    //
    //-----------------------------------------------------------------

    protected SnmpTableSupport(SnmpMib mib){
        theMib=mib;
        meta=getRegisteredTableMeta(mib);
        bindWithTableMeta();
        entries=allocateTable();
    }
    //-----------------------------------------------------------------
    //
    //  Implementation of the SnmpTableEntryFactory interface
    //
    //-----------------------------------------------------------------

    protected abstract SnmpMibTable getRegisteredTableMeta(SnmpMib mib);
    //-----------------------------------------------------------------
    //
    //  Public methods
    //
    //-----------------------------------------------------------------

    protected List<Object> allocateTable(){
        return new ArrayList<Object>();
    }

    protected void bindWithTableMeta(){
        if(meta==null) return;
        registrationRequired=meta.isRegistrationRequired();
        meta.registerEntryFactory(this);
    }

    public abstract void createNewEntry(SnmpMibSubRequest request,
                                        SnmpOid rowOid,int depth,
                                        SnmpMibTable meta)
            throws SnmpStatusException;

    // XXXX xxxx zzz ZZZZ => public? or protected?
    public Object getEntry(int pos){
        if(entries==null) return null;
        return entries.get(pos);
    }

    public int getSize(){
        return meta.getSize();
    }

    public boolean isCreationEnabled(){
        return meta.isCreationEnabled();
    }

    public void setCreationEnabled(boolean remoteCreationFlag){
        meta.setCreationEnabled(remoteCreationFlag);
    }

    public SnmpIndex buildSnmpIndex(SnmpOid rowOid)
            throws SnmpStatusException{
        return buildSnmpIndex(rowOid.longValue(false),0);
    }
    //-----------------------------------------------------------------
    //
    //  Implementation of the SnmpTableEntryFactory interface
    //
    //-----------------------------------------------------------------

    protected abstract SnmpIndex buildSnmpIndex(long oid[],int start)
            throws SnmpStatusException;

    public void addEntryCb(int pos,SnmpOid row,ObjectName name,
                           Object entry,SnmpMibTable meta)
            throws SnmpStatusException{
        try{
            if(entries!=null) entries.add(pos,entry);
        }catch(Exception e){
            throw new SnmpStatusException(SnmpStatusException.noSuchName);
        }
    }

    public void removeEntryCb(int pos,SnmpOid row,ObjectName name,
                              Object entry,SnmpMibTable meta)
            throws SnmpStatusException{
        try{
            if(entries!=null) entries.remove(pos);
        }catch(Exception e){
        }
    }

    public void
    addNotificationListener(NotificationListener listener,
                            NotificationFilter filter,Object handback){
        meta.addNotificationListener(listener,filter,handback);
    }

    public synchronized void
    removeNotificationListener(NotificationListener listener)
            throws ListenerNotFoundException{
        meta.removeNotificationListener(listener);
    }
    //-----------------------------------------------------------------
    //
    //  Protected Abstract methods
    //
    //-----------------------------------------------------------------

    public MBeanNotificationInfo[] getNotificationInfo(){
        return meta.getNotificationInfo();
    }

    protected void addEntry(SnmpIndex index,Object entry)
            throws SnmpStatusException{
        SnmpOid oid=buildOidFromIndex(index);
        ObjectName name=null;
        if(isRegistrationRequired()){
            name=buildNameFromIndex(index);
        }
        meta.addEntry(oid,name,entry);
    }
    //-----------------------------------------------------------------
    //
    //  Protected methods
    //
    //-----------------------------------------------------------------

    public boolean isRegistrationRequired(){
        return registrationRequired;
    }

    public abstract SnmpOid buildOidFromIndex(SnmpIndex index)
            throws SnmpStatusException;

    public abstract ObjectName buildNameFromIndex(SnmpIndex index)
            throws SnmpStatusException;

    protected void addEntry(SnmpIndex index,ObjectName name,Object entry)
            throws SnmpStatusException{
        SnmpOid oid=buildOidFromIndex(index);
        meta.addEntry(oid,name,entry);
    }
    // protected void removeEntry(ObjectName name, Object entry)
    //  throws SnmpStatusException {
    //  meta.removeEntry(name,entry);
    // }

    protected void removeEntry(SnmpIndex index,Object entry)
            throws SnmpStatusException{
        SnmpOid oid=buildOidFromIndex(index);
        meta.removeEntry(oid,entry);
    }

    protected Object[] getBasicEntries(){
        if(entries==null) return null;
        Object[] array=new Object[entries.size()];
        entries.toArray(array);
        return array;
    }
}
