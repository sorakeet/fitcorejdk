/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.interceptors;

import com.sun.corba.se.impl.logging.OMGSystemException;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import com.sun.corba.se.spi.orb.ORB;
import org.omg.CORBA.Any;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.InvalidSlot;

public class PICurrent extends org.omg.CORBA.LocalObject
        implements Current{
    // slotCounter is used to keep track of ORBInitInfo.allocate_slot_id()
    private int slotCounter;
    // The ORB associated with this PICurrent object.
    private ORB myORB;
    private OMGSystemException wrapper;
    // True if the orb is still initialzing and get_slot and set_slot are not
    // to be called.
    private boolean orbInitializing;
    // ThreadLocal contains a stack of SlotTable which are used
    // for resolve_initial_references( "PICurrent" );
    private ThreadLocal threadLocalSlotTable
            =new ThreadLocal(){
        protected Object initialValue(){
            SlotTable table=new SlotTable(myORB,slotCounter);
            return new SlotTableStack(myORB,table);
        }
    };

    PICurrent(ORB myORB){
        this.myORB=myORB;
        wrapper=OMGSystemException.get(myORB,
                CORBALogDomains.RPC_PROTOCOL);
        this.orbInitializing=true;
        slotCounter=0;
    }

    int allocateSlotId(){
        int slotId=slotCounter;
        slotCounter=slotCounter+1;
        return slotId;
    }

    void pushSlotTable(){
        SlotTableStack st=(SlotTableStack)threadLocalSlotTable.get();
        st.pushSlotTable();
    }

    void popSlotTable(){
        SlotTableStack st=(SlotTableStack)threadLocalSlotTable.get();
        st.popSlotTable();
    }

    public Any get_slot(int id) throws InvalidSlot{
        if(orbInitializing){
            // As per ptc/00-08-06 if the ORB is still initializing, disallow
            // calls to get_slot and set_slot.  If an attempt is made to call,
            // throw a BAD_INV_ORDER.
            throw wrapper.invalidPiCall4();
        }
        return getSlotTable().get_slot(id);
    }

    public void set_slot(int id,Any data) throws InvalidSlot{
        if(orbInitializing){
            // As per ptc/00-08-06 if the ORB is still initializing, disallow
            // calls to get_slot and set_slot.  If an attempt is made to call,
            // throw a BAD_INV_ORDER.
            throw wrapper.invalidPiCall3();
        }
        getSlotTable().set_slot(id,data);
    }

    SlotTable getSlotTable(){
        SlotTable table=(SlotTable)
                ((SlotTableStack)threadLocalSlotTable.get()).peekSlotTable();
        return table;
    }

    void resetSlotTable(){
        getSlotTable().resetSlots();
    }

    void setORBInitializing(boolean init){
        this.orbInitializing=init;
    }
}
