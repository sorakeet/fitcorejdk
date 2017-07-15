/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.naming.pcosnaming;
// Import general CORBA classes

import com.sun.corba.se.impl.naming.cosnaming.BindingIteratorImpl;
import org.omg.CORBA.INTERNAL;
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

public class PersistentBindingIterator extends BindingIteratorImpl{
    private POA biPOA;
    private int currentSize;
    private Hashtable theHashtable;
    private Enumeration theEnumeration;
    private ORB orb;

    public PersistentBindingIterator(ORB orb,Hashtable aTable,
                                     POA thePOA) throws Exception{
        super(orb);
        this.orb=orb;
        theHashtable=aTable;
        theEnumeration=this.theHashtable.keys();
        currentSize=this.theHashtable.size();
        biPOA=thePOA;
    }

    final public boolean NextOne(org.omg.CosNaming.BindingHolder b){
        // If there are more elements get the next element
        boolean hasMore=theEnumeration.hasMoreElements();
        if(hasMore){
            InternalBindingKey theBindingKey=
                    ((InternalBindingKey)theEnumeration.nextElement());
            InternalBindingValue theElement=
                    (InternalBindingValue)theHashtable.get(theBindingKey);
            NameComponent n=new NameComponent(theBindingKey.id,theBindingKey.kind);
            NameComponent[] nlist=new NameComponent[1];
            nlist[0]=n;
            BindingType theType=theElement.theBindingType;
            b.value=
                    new Binding(nlist,theType);
        }else{
            // Return empty but marshalable binding
            b.value=new Binding(new NameComponent[0],BindingType.nobject);
        }
        return hasMore;
    }

    final public void Destroy(){
        // Remove the object from the Active Object Map.
        try{
            byte[] objectId=biPOA.servant_to_id(this);
            if(objectId!=null){
                biPOA.deactivate_object(objectId);
            }
        }catch(Exception e){
            throw new INTERNAL("Exception in BindingIterator.Destroy "+e);
        }
    }

    public final int RemainingElements(){
        return currentSize;
    }
}
