/**
 * Copyright (c) 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.impl.presentation.rmi;

import com.sun.corba.se.impl.corba.CORBAObjectImpl;
import com.sun.corba.se.impl.ior.StubIORImpl;
import com.sun.corba.se.impl.logging.UtilSystemException;
import com.sun.corba.se.impl.util.Utility;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import com.sun.corba.se.spi.presentation.rmi.StubAdapter;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.Delegate;
import org.omg.CORBA.portable.ObjectImpl;

import javax.rmi.CORBA.Tie;
import java.rmi.RemoteException;

public abstract class StubConnectImpl{
    static UtilSystemException wrapper=UtilSystemException.get(
            CORBALogDomains.RMIIIOP);

    public static StubIORImpl connect(StubIORImpl ior,org.omg.CORBA.Object proxy,
                                      ObjectImpl stub,ORB orb) throws RemoteException{
        Delegate del=null;
        try{
            try{
                del=StubAdapter.getDelegate(stub);
                if(del.orb(stub)!=orb)
                    throw wrapper.connectWrongOrb();
            }catch(BAD_OPERATION err){
                if(ior==null){
                    // No IOR, can we get a Tie for this stub?
                    Tie tie=(Tie)Utility.getAndForgetTie(proxy);
                    if(tie==null)
                        throw wrapper.connectNoTie();
                    // Is the tie already connected?  If it is, check that it's
                    // connected to the same ORB, otherwise connect it.
                    ORB existingOrb=orb;
                    try{
                        existingOrb=tie.orb();
                    }catch(BAD_OPERATION exc){
                        // Thrown when tie is an ObjectImpl and its delegate is not set.
                        tie.orb(orb);
                    }catch(BAD_INV_ORDER exc){
                        // Thrown when tie is a Servant and its delegate is not set.
                        tie.orb(orb);
                    }
                    if(existingOrb!=orb)
                        throw wrapper.connectTieWrongOrb();
                    // Get the delegate for the stub from the tie.
                    del=StubAdapter.getDelegate(tie);
                    ObjectImpl objref=new CORBAObjectImpl();
                    objref._set_delegate(del);
                    ior=new StubIORImpl(objref);
                }else{
                    // ior is initialized, so convert ior to an object, extract
                    // the delegate, and set it on ourself
                    del=ior.getDelegate(orb);
                }
                StubAdapter.setDelegate(stub,del);
            }
        }catch(SystemException exc){
            throw new RemoteException("CORBA SystemException",exc);
        }
        return ior;
    }
}
