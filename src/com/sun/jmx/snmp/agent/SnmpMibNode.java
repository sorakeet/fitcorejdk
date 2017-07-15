/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;
// java imports
//

import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.SnmpVarBind;

import java.io.Serializable;
import java.util.Vector;
// jmx imports
//

public abstract class SnmpMibNode implements Serializable{
    // ---------------------------------------------------------------------
    // PUBLIC METHODS
    //----------------------------------------------------------------------

    //----------------------------------------------------------------------
    // PROTECTED VARIABLES
    //----------------------------------------------------------------------
    protected int[] varList;

    static public void sort(int array[]){
        QuickSort(array,0,array.length-1);
    }

    static void QuickSort(int a[],int lo0,int hi0){
        int lo=lo0;
        int hi=hi0;
        int mid;
        if(hi0>lo0){
            /** Arbitrarily establishing partition element as the midpoint of
             * the array.
             */
            mid=a[(lo0+hi0)/2];
            // loop through the array until indices cross
            while(lo<=hi){
                /** find the first element that is greater than or equal to
                 * the partition element starting from the left Index.
                 */
                while((lo<hi0)&&(a[lo]<mid))
                    ++lo;
                /** find an element that is smaller than or equal to
                 * the partition element starting from the right Index.
                 */
                while((hi>lo0)&&(a[hi]>mid))
                    --hi;
                // if the indexes have not crossed, swap
                if(lo<=hi){
                    swap(a,lo,hi);
                    ++lo;
                    --hi;
                }
            }
            /** If the right index has not reached the left side of array
             * must now sort the left partition.
             */
            if(lo0<hi)
                QuickSort(a,lo0,hi);
            /** If the left index has not reached the right side of array
             * must now sort the right partition.
             */
            if(lo<hi0)
                QuickSort(a,lo,hi0);
        }
    }

    final static private void swap(int a[],int i,int j){
        int T;
        T=a[i];
        a[i]=a[j];
        a[j]=T;
    }

    public long getNextVarId(long id,Object userData,int pduVersion)
            throws SnmpStatusException{
        long varid=id;
        do{
            varid=getNextVarId(varid,userData);
        }while(skipVariable(varid,userData,pduVersion));
        return varid;
    }

    public long getNextVarId(long id,Object userData)
            throws SnmpStatusException{
        return getNextIdentifier(varList,id);
    }

    final static protected int getNextIdentifier(int table[],long value)
            throws SnmpStatusException{
        final int[] a=table;
        final int val=(int)value;
        if(a==null){
            throw new SnmpStatusException(SnmpStatusException.noSuchObject);
        }
        int low=0;
        int max=a.length;
        int curr=low+(max-low)/2;
        int elmt=0;
        // Basic check
        //
        if(max<1){
            throw new SnmpStatusException(SnmpStatusException.noSuchObject);
        }
        if(a[max-1]<=val){
            throw new SnmpStatusException(SnmpStatusException.noSuchObject);
        }
        while(low<=max){
            elmt=a[curr];
            if(val==elmt){
                // We ned to get the next index ...
                //
                curr++;
                return a[curr];
            }
            if(elmt<val){
                low=curr+1;
            }else{
                max=curr-1;
            }
            curr=low+(max-low)/2;
        }
        return a[curr];
    }

    protected boolean skipVariable(long id,Object userData,int pduVersion){
        return false;
    }

    void findHandlingNode(SnmpVarBind varbind,
                          long[] oid,int depth,
                          SnmpRequestTree handlers)
            throws SnmpStatusException{
        throw new SnmpStatusException(SnmpStatusException.noSuchObject);
    }

    long[] findNextHandlingNode(SnmpVarBind varbind,
                                long[] oid,int pos,int depth,
                                SnmpRequestTree handlers,AcmChecker checker)
            throws SnmpStatusException{
        throw new SnmpStatusException(SnmpStatusException.noSuchObject);
    }
    //----------------------------------------------------------------------
    // PACKAGE METHODS
    //----------------------------------------------------------------------

    public abstract void get(SnmpMibSubRequest req,int depth)
            throws SnmpStatusException;
    //----------------------------------------------------------------------
    // PROTECTED METHODS
    //----------------------------------------------------------------------

    public abstract void set(SnmpMibSubRequest req,int depth)
            throws SnmpStatusException;
    //----------------------------------------------------------------------
    // PRIVATE METHODS
    //----------------------------------------------------------------------

    public abstract void check(SnmpMibSubRequest req,int depth)
            throws SnmpStatusException;

    public void getRootOid(Vector<Integer> result){
        return;
    }
}
