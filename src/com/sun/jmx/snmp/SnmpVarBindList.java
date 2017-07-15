/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Copyright (c) 1995-96 by Cisco Systems, Inc.
package com.sun.jmx.snmp;

import java.util.Enumeration;
import java.util.Vector;

public class SnmpVarBindList extends Vector<SnmpVarBind>{
    private static final long serialVersionUID=-7203997794636430321L;
    public String identity="VarBindList ";   // name identifying this list.
    Timestamp timestamp;
    // CONSTRUCTORS
    //-------------

    public SnmpVarBindList(){
        super(5,5);
    }

    public SnmpVarBindList(int initialCapacity){
        super(initialCapacity);
    }

    public SnmpVarBindList(String name){
        super(5,5);
        identity=name;
    }

    public SnmpVarBindList(SnmpVarBindList list){
        super(list.size(),5);
        list.copyInto(elementData);
        elementCount=list.size();
    }

    public SnmpVarBindList(String name,Vector<SnmpVarBind> list){
        this(list);
        identity=name;
    }

    public SnmpVarBindList(Vector<SnmpVarBind> list){
        super(list.size(),5);
        for(Enumeration<SnmpVarBind> e=list.elements();e.hasMoreElements();){
            final SnmpVarBind varBind=e.nextElement();
            addElement(varBind.clone());
        }
    }
    // GETTER/SETTER
    //--------------

    public final synchronized SnmpVarBind getVarBindAt(int pos){
        return elementAt(pos);
    }

    public synchronized int getVarBindCount(){
        return size();
    }

    public synchronized Enumeration<SnmpVarBind> getVarBindList(){
        return elements();
    }

    public final synchronized void setVarBindList(Vector<SnmpVarBind> list){
        setVarBindList(list,false);
    }

    public final synchronized void setVarBindList(Vector<SnmpVarBind> list,boolean copy){
        synchronized(list){
            final int max=list.size();
            setSize(max);
            list.copyInto(this.elementData);
            if(copy){         // do deepcopy of all vars.
                for(int i=0;i<max;i++){
                    SnmpVarBind avar=(SnmpVarBind)elementData[i];
                    elementData[i]=avar.clone();
                }
            }
        }
    }

    public synchronized void addVarBindList(SnmpVarBindList list){
        ensureCapacity(list.size()+size());
        for(int i=0;i<list.size();i++){
            addElement(list.getVarBindAt(i));
        }
    }

    public synchronized boolean removeVarBindList(SnmpVarBindList list){
        boolean result=true;
        for(int i=0;i<list.size();i++){
            result=removeElement(list.getVarBindAt(i));
        }
        return result;
    }
    // PUBLIC METHODS
    //---------------

    public final synchronized void replaceVarBind(SnmpVarBind var,int pos){
        setElementAt(var,pos);
    }

    public synchronized void addVarBind(String list[]) throws SnmpStatusException{
        addVarBind(list,null);
    }

    public final synchronized void addVarBind(String list[],String inst) throws SnmpStatusException{
        for(int i=0;i<list.length;i++){
            SnmpVarBind avar=new SnmpVarBind(list[i]);
            avar.addInstance(inst);
            addElement(avar);
        }
    }

    public synchronized boolean removeVarBind(String list[]) throws SnmpStatusException{
        return removeVarBind(list,null);
    }

    public synchronized boolean removeVarBind(String list[],String inst) throws SnmpStatusException{
        boolean result=true;
        for(int i=0;i<list.length;i++){
            SnmpVarBind avar=new SnmpVarBind(list[i]);
            avar.addInstance(inst);
            int indexOid=indexOfOid(avar);
            try{
                removeElementAt(indexOid);
            }catch(ArrayIndexOutOfBoundsException e){
                result=false;
            }
        }
        return result;
    }

    public synchronized int indexOfOid(SnmpVarBind var){
        return indexOfOid(var,0,size());
    }

    public synchronized int indexOfOid(SnmpVarBind var,int min,int max){
        SnmpOid oidarg=var.getOid();
        for(int i=min;i<max;i++){
            SnmpVarBind avar=(SnmpVarBind)elementData[i];
            if(oidarg.equals(avar.getOid()))
                return i;
        }
        return -1;
    }

    public synchronized void addVarBind(String name) throws SnmpStatusException{
        SnmpVarBind avar;
        avar=new SnmpVarBind(name);
        addVarBind(avar);
    }

    public synchronized void addVarBind(SnmpVarBind var){
        addElement(var);
    }

    public synchronized boolean removeVarBind(String name) throws SnmpStatusException{
        SnmpVarBind avar;
        int indexOid;
        avar=new SnmpVarBind(name);
        indexOid=indexOfOid(avar);
        try{
            removeElementAt(indexOid);
            return true;
        }catch(ArrayIndexOutOfBoundsException e){
            return false;
        }
    }

    public synchronized boolean removeVarBind(SnmpVarBind var){
        return removeElement(var);
    }

    public synchronized void addInstance(String inst) throws SnmpStatusException{
        int max=size();
        for(int i=0;i<max;i++){
            ((SnmpVarBind)elementData[i]).addInstance(inst);
        }
    }

    final public synchronized void concat(Vector<SnmpVarBind> list){
        ensureCapacity(size()+list.size());
        for(Enumeration<SnmpVarBind> e=list.elements();e.hasMoreElements();){
            addElement(e.nextElement());
        }
    }

    public synchronized boolean checkForValidValues(){
        int max=this.size();
        for(int i=0;i<max;i++){
            SnmpVarBind avar=(SnmpVarBind)elementData[i];
            if(avar.isValidValue()==false)
                return false;
        }
        return true;
    }

    public synchronized boolean checkForUnspecifiedValue(){
        int max=this.size();
        for(int i=0;i<max;i++){
            SnmpVarBind avar=(SnmpVarBind)elementData[i];
            if(avar.isUnspecifiedValue())
                return true;
        }
        return false;
    }

    public synchronized SnmpVarBindList splitAt(int pos){
        SnmpVarBindList splitVb=null;
        if(pos>elementCount)
            return splitVb;
        splitVb=new SnmpVarBindList(); // size() - atPosition) ;
        int max=size();
        for(int i=pos;i<max;i++)
            splitVb.addElement((SnmpVarBind)elementData[i]);
        elementCount=pos;
        trimToSize();
        return splitVb;
    }

    public synchronized int indexOfOid(SnmpOid oid){
        int max=size();
        for(int i=0;i<max;i++){
            SnmpVarBind avar=(SnmpVarBind)elementData[i];
            if(oid.equals(avar.getOid()))
                return i;
        }
        return -1;
    }

    public synchronized SnmpVarBindList cloneWithoutValue(){
        SnmpVarBindList newvb=new SnmpVarBindList();
        int max=this.size();
        newvb.ensureCapacity(max);
        for(int i=0;i<max;i++){
            SnmpVarBind avar=(SnmpVarBind)elementData[i];
            newvb.addElement((SnmpVarBind)avar.cloneWithoutValue());
        }
        return newvb;
    }

    @Override
    public synchronized SnmpVarBindList clone(){
        return cloneWithValue();
    }

    public synchronized SnmpVarBindList cloneWithValue(){
        SnmpVarBindList newvb=new SnmpVarBindList();
        newvb.setTimestamp(this.getTimestamp());
        newvb.ensureCapacity(this.size());
        for(int i=0;i<this.size();i++){
            SnmpVarBind avar=(SnmpVarBind)elementData[i];
            newvb.addElement(avar.clone());
        }
        return newvb;
    }

    public Timestamp getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(Timestamp tstamp){
        timestamp=tstamp;
    }

    public synchronized Vector<SnmpVarBind> toVector(boolean copy){
        final int count=elementCount;
        if(copy==false) return new Vector<>(this);
        Vector<SnmpVarBind> result=new Vector<>(count,5);
        for(int i=0;i<count;i++){
            SnmpVarBind avar=(SnmpVarBind)elementData[i];
            result.addElement(avar.clone());
        }
        return result;
    }

    public String oidListToString(){
        StringBuilder s=new StringBuilder(300);
        for(int i=0;i<elementCount;i++){
            SnmpVarBind avar=(SnmpVarBind)elementData[i];
            s.append(avar.getOid().toString()).append("\n");
        }
        return s.toString();
    }

    public synchronized String varBindListToString(){
        StringBuilder s=new StringBuilder(300);
        for(int i=0;i<elementCount;i++){
            s.append(elementData[i].toString()).append("\n");
        }
        return s.toString();
    }

    @Override
    protected void finalize(){
        removeAllElements();
    }
}
