/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.interceptors;

import com.sun.corba.se.impl.corba.AnyImpl;
import com.sun.corba.se.spi.orb.ORB;
import org.omg.CORBA.Any;
import org.omg.PortableInterceptor.InvalidSlot;

public class SlotTable{
    // The vector where all the slot data for the current thread is stored
    private Any[] theSlotData;
    // Required for instantiating Any object.
    private ORB orb;
    // The flag to check whether there are any updates in the current SlotTable.
    // The slots will be reset to null, only if this flag is set.
    private boolean dirtyFlag;

    SlotTable(ORB orb,int slotSize){
        dirtyFlag=false;
        this.orb=orb;
        theSlotData=new Any[slotSize];
    }

    public void set_slot(int id,Any data) throws InvalidSlot{
        // First check whether the slot is allocated
        // If not, raise the invalid slot exception
        if(id>=theSlotData.length){
            throw new InvalidSlot();
        }
        dirtyFlag=true;
        theSlotData[id]=data;
    }

    public Any get_slot(int id) throws InvalidSlot{
        // First check whether the slot is allocated
        // If not, raise the invalid slot exception
        if(id>=theSlotData.length){
            throw new InvalidSlot();
        }
        if(theSlotData[id]==null){
            theSlotData[id]=new AnyImpl(orb);
        }
        return theSlotData[id];
    }

    void resetSlots(){
        if(dirtyFlag==true){
            for(int i=0;i<theSlotData.length;i++){
                theSlotData[i]=null;
            }
        }
    }

    int getSize(){
        return theSlotData.length;
    }
}
