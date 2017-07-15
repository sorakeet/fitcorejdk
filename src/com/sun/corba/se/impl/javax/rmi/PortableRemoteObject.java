/**
 * Copyright (c) 1999, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 */
/**
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 */
package com.sun.corba.se.impl.javax.rmi;

import com.sun.corba.se.impl.util.RepositoryId;
import com.sun.corba.se.impl.util.Utility;
import com.sun.corba.se.spi.presentation.rmi.StubAdapter;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;

import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.Util;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.rmi.server.RemoteStub;
import java.rmi.server.UnicastRemoteObject;

public class PortableRemoteObject
        implements javax.rmi.CORBA.PortableRemoteObjectDelegate{
    public void exportObject(Remote obj)
            throws RemoteException{
        if(obj==null){
            throw new NullPointerException("invalid argument");
        }
        // Has this object already been exported to IIOP?
        if(Util.getTie(obj)!=null){
            // Yes, so this is an error...
            throw new ExportException(obj.getClass().getName()+" already exported");
        }
        // Can we load a Tie?
        Tie theTie=Utility.loadTie(obj);
        if(theTie!=null){
            // Yes, so export it to IIOP...
            Util.registerTarget(theTie,obj);
        }else{
            // No, so export to JRMP. If this is called twice for the
            // same object, it will throw an ExportException...
            UnicastRemoteObject.exportObject(obj);
        }
    }

    public Remote toStub(Remote obj)
            throws NoSuchObjectException{
        Remote result=null;
        if(obj==null){
            throw new NullPointerException("invalid argument");
        }
        // If the class is already an IIOP stub then return it.
        if(StubAdapter.isStub(obj)){
            return obj;
        }
        // If the class is already a JRMP stub then return it.
        if(obj instanceof RemoteStub){
            return obj;
        }
        // Has it been exported to IIOP?
        Tie theTie=Util.getTie(obj);
        if(theTie!=null){
            result=Utility.loadStub(theTie,null,null,true);
        }else{
            if(Utility.loadTie(obj)==null){
                result=java.rmi.server.RemoteObject.toStub(obj);
            }
        }
        if(result==null){
            throw new NoSuchObjectException("object not exported");
        }
        return result;
    }

    public void unexportObject(Remote obj)
            throws NoSuchObjectException{
        if(obj==null){
            throw new NullPointerException("invalid argument");
        }
        if(StubAdapter.isStub(obj)||
                obj instanceof RemoteStub){
            throw new NoSuchObjectException(
                    "Can only unexport a server object.");
        }
        Tie theTie=Util.getTie(obj);
        if(theTie!=null){
            Util.unexportObject(obj);
        }else{
            if(Utility.loadTie(obj)==null){
                UnicastRemoteObject.unexportObject(obj,true);
            }else{
                throw new NoSuchObjectException("Object not exported.");
            }
        }
    }

    public Object narrow(Object narrowFrom,
                         Class narrowTo) throws ClassCastException{
        Object result=null;
        if(narrowFrom==null)
            return null;
        if(narrowTo==null)
            throw new NullPointerException("invalid argument");
        try{
            if(narrowTo.isAssignableFrom(narrowFrom.getClass()))
                return narrowFrom;
            // Is narrowTo an interface that might be
            // implemented by a servant running on iiop?
            if(narrowTo.isInterface()&&
                    narrowTo!=java.io.Serializable.class&&
                    narrowTo!=java.io.Externalizable.class){
                org.omg.CORBA.Object narrowObj
                        =(org.omg.CORBA.Object)narrowFrom;
                // Create an id from the narrowTo type...
                String id=RepositoryId.createForAnyType(narrowTo);
                if(narrowObj._is_a(id)){
                    return Utility.loadStub(narrowObj,narrowTo);
                }else{
                    throw new ClassCastException("Object is not of remote type "+
                            narrowTo.getName());
                }
            }else{
                throw new ClassCastException("Class "+narrowTo.getName()+
                        " is not a valid remote interface");
            }
        }catch(Exception error){
            ClassCastException cce=new ClassCastException();
            cce.initCause(error);
            throw cce;
        }
    }

    public void connect(Remote target,Remote source)
            throws RemoteException{
        if(target==null||source==null){
            throw new NullPointerException("invalid argument");
        }
        ORB orb=null;
        try{
            if(StubAdapter.isStub(source)){
                orb=StubAdapter.getORB(source);
            }else{
                // Is this a servant that was exported to iiop?
                Tie tie=Util.getTie(source);
                if(tie==null){
                    /** loadTie always succeeds for dynamic RMI-IIOP
                     // No, can we get a tie for it?  If not,
                     // assume that source is a JRMP object...
                     if (Utility.loadTie(source) != null) {
                     // Yes, so it is an iiop object which
                     // has not been exported...
                     throw new RemoteException(
                     "'source' object not exported");
                     }
                     */
                }else{
                    orb=tie.orb();
                }
            }
        }catch(SystemException e){
            throw new RemoteException("'source' object not connected",e);
        }
        boolean targetIsIIOP=false;
        Tie targetTie=null;
        if(StubAdapter.isStub(target)){
            targetIsIIOP=true;
        }else{
            targetTie=Util.getTie(target);
            if(targetTie!=null){
                targetIsIIOP=true;
            }else{
                /** loadTie always succeeds for dynamic RMI-IIOP
                 if (Utility.loadTie(target) != null) {
                 throw new RemoteException("'target' servant not exported");
                 }
                 */
            }
        }
        if(!targetIsIIOP){
            // Yes. Do we have an ORB from the source object?
            // If not, we're done - there is nothing to do to
            // connect a JRMP object. If so, it is an error because
            // the caller mixed JRMP and IIOP...
            if(orb!=null){
                throw new RemoteException(
                        "'source' object exported to IIOP, 'target' is JRMP");
            }
        }else{
            // The target object is IIOP. Make sure we have a
            // valid ORB from the source object...
            if(orb==null){
                throw new RemoteException(
                        "'source' object is JRMP, 'target' is IIOP");
            }
            // And, finally, connect it up...
            try{
                if(targetTie!=null){
                    // Is the tie already connected?
                    try{
                        ORB existingOrb=targetTie.orb();
                        // Yes. Is it the same orb?
                        if(existingOrb==orb){
                            // Yes, so nothing to do...
                            return;
                        }else{
                            // No, so this is an error...
                            throw new RemoteException(
                                    "'target' object was already connected");
                        }
                    }catch(SystemException e){
                    }
                    // No, so do it...
                    targetTie.orb(orb);
                }else{
                    StubAdapter.connect(target,orb);
                }
            }catch(SystemException e){
                // The stub or tie was already connected...
                throw new RemoteException(
                        "'target' object was already connected",e);
            }
        }
    }
}
