/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

import sun.rmi.server.UnicastServerRef;
import sun.rmi.server.UnicastServerRef2;

import java.rmi.Remote;
import java.rmi.RemoteException;

public class UnicastRemoteObject extends RemoteServer{
    private static final long serialVersionUID=4974527148936298033L;
    private int port=0;
    private RMIClientSocketFactory csf=null;
    private RMIServerSocketFactory ssf=null;

    protected UnicastRemoteObject() throws RemoteException{
        this(0);
    }

    protected UnicastRemoteObject(int port) throws RemoteException{
        this.port=port;
        exportObject((Remote)this,port);
    }

    public static Remote exportObject(Remote obj,int port)
            throws RemoteException{
        return exportObject(obj,new UnicastServerRef(port));
    }

    private static Remote exportObject(Remote obj,UnicastServerRef sref)
            throws RemoteException{
        // if obj extends UnicastRemoteObject, set its ref.
        if(obj instanceof UnicastRemoteObject){
            ((UnicastRemoteObject)obj).ref=sref;
        }
        return sref.exportObject(obj,null,false);
    }

    protected UnicastRemoteObject(int port,
                                  RMIClientSocketFactory csf,
                                  RMIServerSocketFactory ssf)
            throws RemoteException{
        this.port=port;
        this.csf=csf;
        this.ssf=ssf;
        exportObject((Remote)this,port,csf,ssf);
    }

    public static Remote exportObject(Remote obj,int port,
                                      RMIClientSocketFactory csf,
                                      RMIServerSocketFactory ssf)
            throws RemoteException{
        return exportObject(obj,new UnicastServerRef2(port,csf,ssf));
    }

    @Deprecated
    public static RemoteStub exportObject(Remote obj)
            throws RemoteException{
        /**
         * Use UnicastServerRef constructor passing the boolean value true
         * to indicate that only a generated stub class should be used.  A
         * generated stub class must be used instead of a dynamic proxy
         * because the return value of this method is RemoteStub which a
         * dynamic proxy class cannot extend.
         */
        return (RemoteStub)exportObject(obj,new UnicastServerRef(true));
    }

    public static boolean unexportObject(Remote obj,boolean force)
            throws java.rmi.NoSuchObjectException{
        return sun.rmi.transport.ObjectTable.unexportObject(obj,force);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException{
        in.defaultReadObject();
        reexport();
    }

    private void reexport() throws RemoteException{
        if(csf==null&&ssf==null){
            exportObject((Remote)this,port);
        }else{
            exportObject((Remote)this,port,csf,ssf);
        }
    }

    public Object clone() throws CloneNotSupportedException{
        try{
            UnicastRemoteObject cloned=(UnicastRemoteObject)super.clone();
            cloned.reexport();
            return cloned;
        }catch(RemoteException e){
            throw new ServerCloneException("Clone failed",e);
        }
    }
}
