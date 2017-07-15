/**
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.naming.cosnaming;
// Import general CORBA classes

import org.omg.CORBA.ORB;
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.NameComponent;
import org.omg.PortableServer.POA;

import java.util.Enumeration;
import java.util.Hashtable;
// Get org.omg.CosNaming Types
// Get base implementation
// Get a hash table

public class TransientBindingIterator extends BindingIteratorImpl{
    // There is only one POA used for both TransientNamingContext and
    // TransientBindingIteraor servants.
    private POA nsPOA;
    private int currentSize;
    private Hashtable theHashtable;
    private Enumeration theEnumeration;

    public TransientBindingIterator(ORB orb,Hashtable aTable,
                                    POA thePOA)
            throws Exception{
        super(orb);
        theHashtable=aTable;
        theEnumeration=this.theHashtable.elements();
        currentSize=this.theHashtable.size();
        this.nsPOA=thePOA;
    }

    final public boolean NextOne(org.omg.CosNaming.BindingHolder b){
        // If there are more elements get the next element
        boolean hasMore=theEnumeration.hasMoreElements();
        if(hasMore){
            b.value=
                    ((InternalBindingValue)theEnumeration.nextElement()).theBinding;
            currentSize--;
        }else{
            // Return empty but marshalable binding
            b.value=new Binding(new NameComponent[0],BindingType.nobject);
        }
        return hasMore;
    }

    final public void Destroy(){
        // Remove the object from the Active Object Map.
        try{
            byte[] objectId=nsPOA.servant_to_id(this);
            if(objectId!=null){
                nsPOA.deactivate_object(objectId);
            }
        }catch(Exception e){
            NamingUtils.errprint("BindingIterator.Destroy():caught exception:");
            NamingUtils.printException(e);
        }
    }

    public final int RemainingElements(){
        return currentSize;
    }
}
