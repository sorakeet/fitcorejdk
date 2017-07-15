/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.interceptors;

import com.sun.corba.se.impl.logging.InterceptorsSystemException;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import com.sun.corba.se.spi.orb.ORB;

public class SlotTableStack{
    // Contains all the active SlotTables for each thread.
    // The List is made to behave like a stack.
    private java.util.List tableContainer;
    // Keeps track of number of PICurrents in the stack.
    private int currentIndex;
    // For Every Thread there will be a pool of re-usable SlotTables'
    // stored in SlotTablePool
    private SlotTablePool tablePool;
    // The ORB associated with this slot table stack
    private ORB orb;
    private InterceptorsSystemException wrapper;
    SlotTableStack(ORB orb,SlotTable table){
        this.orb=orb;
        wrapper=InterceptorsSystemException.get(orb,CORBALogDomains.RPC_PROTOCOL);
        currentIndex=0;
        tableContainer=new java.util.ArrayList();
        tablePool=new SlotTablePool();
        // SlotTableStack will be created with one SlotTable on the stack.
        // This table is used as the reference to query for number of
        // allocated slots to create other slottables.
        tableContainer.add(currentIndex,table);
        currentIndex++;
    }

    void pushSlotTable(){
        SlotTable table=tablePool.getSlotTable();
        if(table==null){
            // get an existing PICurrent to get the slotSize
            SlotTable tableTemp=peekSlotTable();
            table=new SlotTable(orb,tableTemp.getSize());
        }
        // NOTE: Very important not to always "add" - otherwise a memory leak.
        if(currentIndex==tableContainer.size()){
            // Add will cause the table to grow.
            tableContainer.add(currentIndex,table);
        }else if(currentIndex>tableContainer.size()){
            throw wrapper.slotTableInvariant(new Integer(currentIndex),
                    new Integer(tableContainer.size()));
        }else{
            // Set will override unused slots.
            tableContainer.set(currentIndex,table);
        }
        currentIndex++;
    }

    SlotTable peekSlotTable(){
        return (SlotTable)tableContainer.get(currentIndex-1);
    }

    void popSlotTable(){
        if(currentIndex<=1){
            // Do not pop the SlotTable, If there is only one.
            // This should not happen, But an extra check for safety.
            throw wrapper.cantPopOnlyPicurrent();
        }
        currentIndex--;
        SlotTable table=(SlotTable)tableContainer.get(currentIndex);
        tableContainer.set(currentIndex,null); // Do not leak memory.
        table.resetSlots();
        tablePool.putSlotTable(table);
    }

    // SlotTablePool is the container for reusable SlotTables'
    private class SlotTablePool{
        // High water mark for the pool
        // If the pool size reaches this limit then putSlotTable will
        // not put SlotTable to the pool.
        private final int HIGH_WATER_MARK=5;
        // Contains a list of reusable SlotTable
        private SlotTable[] pool;
        // currentIndex points to the last SlotTable in the list
        private int currentIndex;

        SlotTablePool(){
            pool=new SlotTable[HIGH_WATER_MARK];
            currentIndex=0;
        }

        void putSlotTable(SlotTable table){
            // If there are enough SlotTables in the pool, then don't add
            // this table to the pool.
            if(currentIndex>=HIGH_WATER_MARK){
                // Let the garbage collector collect it.
                return;
            }
            pool[currentIndex]=table;
            currentIndex++;
        }

        SlotTable getSlotTable(){
            // If there are no entries in the pool then return null
            if(currentIndex==0){
                return null;
            }
            // Works like a stack, Gets the last one added first
            currentIndex--;
            return pool[currentIndex];
        }
    }
}
// End of file.
