/**
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.naming.cosnaming;
// Import general CORBA classes

import com.sun.corba.se.impl.logging.NamingSystemException;
import com.sun.corba.se.impl.orbutil.LogKeywords;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import org.omg.CORBA.Object;
import org.omg.CORBA.SystemException;
import org.omg.CosNaming.*;
import org.omg.PortableServer.POA;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
// Import org.omg.CosNaming types

public class TransientNamingContext extends NamingContextImpl implements NamingContextDataStore{
    // A hashtable to store the bindings
    private final Hashtable theHashtable=new Hashtable();
    public Object localRoot;
    private Logger readLogger, updateLogger, lifecycleLogger;
    // XXX: the wrapper calls are all preceded by logger updates.
    // These can be combined, and then we simply use 3 NamingSystemException wrappers,
    // for read, update, and lifecycl.
    private NamingSystemException wrapper;

    public TransientNamingContext(com.sun.corba.se.spi.orb.ORB orb,
                                  Object initial,
                                  POA nsPOA)
            throws Exception{
        super(orb,nsPOA);
        wrapper=NamingSystemException.get(orb,CORBALogDomains.NAMING);
        this.localRoot=initial;
        readLogger=orb.getLogger(CORBALogDomains.NAMING_READ);
        updateLogger=orb.getLogger(CORBALogDomains.NAMING_UPDATE);
        lifecycleLogger=orb.getLogger(
                CORBALogDomains.NAMING_LIFECYCLE);
        lifecycleLogger.fine("Root TransientNamingContext LIFECYCLE.CREATED");
    }

    public final void Bind(NameComponent n,Object obj,
                           BindingType bt)
            throws SystemException{
        // Create a key and a value
        InternalBindingKey key=new InternalBindingKey(n);
        NameComponent[] name=new NameComponent[1];
        name[0]=n;
        Binding b=new Binding(name,bt);
        InternalBindingValue value=new InternalBindingValue(b,null);
        value.theObjectRef=obj;
        // insert it
        InternalBindingValue oldValue=
                (InternalBindingValue)this.theHashtable.put(key,value);
        if(oldValue!=null){
            updateLogger.warning(LogKeywords.NAMING_BIND+"Name "+
                    getName(n)+" Was Already Bound");
            throw wrapper.transNcBindAlreadyBound();
        }
        if(updateLogger.isLoggable(Level.FINE)){
            updateLogger.fine(LogKeywords.NAMING_BIND_SUCCESS+
                    "Name Component: "+n.id+"."+n.kind);
        }
    }

    public final Object Resolve(NameComponent n,
                                BindingTypeHolder bth)
            throws SystemException{
        // Is the initial naming context requested?
        if((n.id.length()==0)
                &&(n.kind.length()==0)){
            bth.value=BindingType.ncontext;
            return localRoot;
        }
        // Create a key and lookup the value
        InternalBindingKey key=new InternalBindingKey(n);
        InternalBindingValue value=
                (InternalBindingValue)this.theHashtable.get(key);
        if(value==null) return null;
        if(readLogger.isLoggable(Level.FINE)){
            readLogger.fine(LogKeywords.NAMING_RESOLVE_SUCCESS
                    +"Namecomponent :"+getName(n));
        }
        // Copy out binding type and object reference
        bth.value=value.theBinding.binding_type;
        return value.theObjectRef;
    }

    public final Object Unbind(NameComponent n)
            throws SystemException{
        // Create a key and remove it from the hashtable
        InternalBindingKey key=new InternalBindingKey(n);
        InternalBindingValue value=
                (InternalBindingValue)this.theHashtable.remove(key);
        // Return what was found
        if(value==null){
            if(updateLogger.isLoggable(Level.FINE)){
                updateLogger.fine(LogKeywords.NAMING_UNBIND_FAILURE+
                        " There was no binding with the name "+getName(n)+
                        " to Unbind ");
            }
            return null;
        }else{
            if(updateLogger.isLoggable(Level.FINE)){
                updateLogger.fine(LogKeywords.NAMING_UNBIND_SUCCESS+
                        " NameComponent:  "+getName(n));
            }
            return value.theObjectRef;
        }
    }

    public final void List(int how_many,BindingListHolder bl,
                           BindingIteratorHolder bi)
            throws SystemException{
        try{
            // Create a new binding iterator servant with a copy of this
            // hashtable. nsPOA is passed to the object so that it can
            // de-activate itself from the Active Object Map when
            // Binding Iterator.destroy is called.
            TransientBindingIterator bindingIterator=
                    new TransientBindingIterator(this.orb,
                            (Hashtable)this.theHashtable.clone(),nsPOA);
            // Have it set the binding list
            bindingIterator.list(how_many,bl);
            byte[] objectId=nsPOA.activate_object(bindingIterator);
            Object obj=nsPOA.id_to_reference(objectId);
            // Get the object reference for the binding iterator servant
            org.omg.CosNaming.BindingIterator bindingRef=
                    org.omg.CosNaming.BindingIteratorHelper.narrow(obj);
            bi.value=bindingRef;
        }catch(SystemException e){
            readLogger.warning(LogKeywords.NAMING_LIST_FAILURE+e);
            throw e;
        }catch(Exception e){
            // Convert to a CORBA system exception
            readLogger.severe(LogKeywords.NAMING_LIST_FAILURE+e);
            throw wrapper.transNcListGotExc(e);
        }
    }

    public final NamingContext NewContext()
            throws SystemException{
        try{
            // Create a new servant
            TransientNamingContext transContext=
                    new TransientNamingContext(
                            (com.sun.corba.se.spi.orb.ORB)orb,localRoot,nsPOA);
            byte[] objectId=nsPOA.activate_object(transContext);
            Object obj=nsPOA.id_to_reference(objectId);
            lifecycleLogger.fine("TransientNamingContext "+
                    "LIFECYCLE.CREATE SUCCESSFUL");
            return org.omg.CosNaming.NamingContextHelper.narrow(obj);
        }catch(SystemException e){
            lifecycleLogger.log(
                    Level.WARNING,LogKeywords.LIFECYCLE_CREATE_FAILURE,e);
            throw e;
        }catch(Exception e){
            lifecycleLogger.log(
                    Level.WARNING,LogKeywords.LIFECYCLE_CREATE_FAILURE,e);
            throw wrapper.transNcNewctxGotExc(e);
        }
    }

    public final void Destroy()
            throws SystemException{
        // Destroy the object reference by disconnecting from the ORB
        try{
            byte[] objectId=nsPOA.servant_to_id(this);
            if(objectId!=null){
                nsPOA.deactivate_object(objectId);
            }
            if(lifecycleLogger.isLoggable(Level.FINE)){
                lifecycleLogger.fine(
                        LogKeywords.LIFECYCLE_DESTROY_SUCCESS);
            }
        }catch(SystemException e){
            lifecycleLogger.log(Level.WARNING,
                    LogKeywords.LIFECYCLE_DESTROY_FAILURE,e);
            throw e;
        }catch(Exception e){
            lifecycleLogger.log(Level.WARNING,
                    LogKeywords.LIFECYCLE_DESTROY_FAILURE,e);
            throw wrapper.transNcDestroyGotExc(e);
        }
    }

    public final boolean IsEmpty(){
        return this.theHashtable.isEmpty();
    }

    private String getName(NameComponent n){
        return n.id+"."+n.kind;
    }
}
