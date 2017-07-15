/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.corba.se.impl.javax.rmi.CORBA;

import com.sun.corba.se.impl.ior.StubIORImpl;
import com.sun.corba.se.impl.logging.UtilSystemException;
import com.sun.corba.se.impl.presentation.rmi.StubConnectImpl;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import org.omg.CORBA.ORB;

import java.io.IOException;
import java.rmi.RemoteException;

public class StubDelegateImpl implements javax.rmi.CORBA.StubDelegate{
    static UtilSystemException wrapper=UtilSystemException.get(
            CORBALogDomains.RMIIIOP);
    private StubIORImpl ior;

    public StubDelegateImpl(){
        ior=null;
    }

    public StubIORImpl getIOR(){
        return ior;
    }

    public int hashCode(javax.rmi.CORBA.Stub self){
        init(self);
        return ior.hashCode();
    }

    private void init(javax.rmi.CORBA.Stub self){
        // If the Stub is not connected to an ORB, BAD_OPERATION exception
        // will be raised by the code below.
        if(ior==null)
            ior=new StubIORImpl(self);
    }

    public boolean equals(javax.rmi.CORBA.Stub self,Object obj){
        if(self==obj){
            return true;
        }
        if(!(obj instanceof javax.rmi.CORBA.Stub)){
            return false;
        }
        // no need to call init() because of calls to hashCode() below
        javax.rmi.CORBA.Stub other=(javax.rmi.CORBA.Stub)obj;
        if(other.hashCode()!=self.hashCode()){
            return false;
        }
        // hashCodes being the same does not mean equality. The stubs still
        // could be pointing to different IORs. So, do a literal comparison.
        // Apparently the ONLY way to do this (other than using private
        // reflection)  toString, because it is not possible to directly
        // access the StubDelegateImpl from the Stub.
        return self.toString().equals(other.toString());
    }

    public String toString(javax.rmi.CORBA.Stub self){
        if(ior==null)
            return null;
        else
            return ior.toString();
    }

    public void connect(javax.rmi.CORBA.Stub self,ORB orb)
            throws RemoteException{
        ior=StubConnectImpl.connect(ior,self,self,orb);
    }

    public void readObject(javax.rmi.CORBA.Stub self,
                           java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException{
        if(ior==null)
            ior=new StubIORImpl();
        ior.doRead(stream);
    }

    public void writeObject(javax.rmi.CORBA.Stub self,
                            java.io.ObjectOutputStream stream) throws IOException{
        init(self);
        ior.doWrite(stream);
    }

    public int hashCode(){
        if(ior==null){
            return 0;
        }else{
            return ior.hashCode();
        }
    }

    public boolean equals(Object obj){
        if(this==obj)
            return true;
        if(!(obj instanceof StubDelegateImpl))
            return false;
        StubDelegateImpl other=(StubDelegateImpl)obj;
        if(ior==null)
            return ior==other.ior;
        else
            return ior.equals(other.ior);
    }
}
