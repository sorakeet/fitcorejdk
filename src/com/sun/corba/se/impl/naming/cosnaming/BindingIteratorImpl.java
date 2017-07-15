/**
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.naming.cosnaming;
// Import general CORBA classes

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingHolder;
import org.omg.CosNaming.BindingIteratorPOA;
import org.omg.CosNaming.BindingListHolder;
// Import org.omg.CosNaming classes

public abstract class BindingIteratorImpl extends BindingIteratorPOA{
    protected ORB orb;

    public BindingIteratorImpl(ORB orb)
            throws Exception{
        super();
        this.orb=orb;
    }

    public synchronized boolean next_one(BindingHolder b){
        // NextOne actually returns the next one
        return NextOne(b);
    }

    public synchronized boolean next_n(int how_many,
                                       BindingListHolder blh){
        if(how_many==0){
            throw new BAD_PARAM(" 'how_many' parameter is set to 0 which is"+
                    " invalid");
        }
        return list(how_many,blh);
    }

    public boolean list(int how_many,BindingListHolder blh){
        // Take the smallest of what's left and what's being asked for
        int numberToGet=Math.min(RemainingElements(),how_many);
        // Create a resulting BindingList
        Binding[] bl=new Binding[numberToGet];
        BindingHolder bh=new BindingHolder();
        int i=0;
        // Keep iterating as long as there are entries
        while(i<numberToGet&&this.NextOne(bh)==true){
            bl[i]=bh.value;
            i++;
        }
        // Found any at all?
        if(i==0){
            // No
            blh.value=new Binding[0];
            return false;
        }
        // Set into holder
        blh.value=bl;
        return true;
    }

    protected abstract int RemainingElements();

    public synchronized void destroy(){
        // Destroy actually destroys
        this.Destroy();
    }

    protected abstract void Destroy();

    protected abstract boolean NextOne(BindingHolder b);
}
