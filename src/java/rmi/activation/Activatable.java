/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.activation;

import sun.rmi.server.ActivatableServerRef;

import java.rmi.MarshalledObject;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteServer;

public abstract class Activatable extends RemoteServer{
    private static final long serialVersionUID=-3120617863591563455L;
    private ActivationID id;

    protected Activatable(String location,
                          MarshalledObject<?> data,
                          boolean restart,
                          int port)
            throws ActivationException, RemoteException{
        super();
        id=exportObject(this,location,data,restart,port);
    }

    public static ActivationID exportObject(Remote obj,
                                            String location,
                                            MarshalledObject<?> data,
                                            boolean restart,
                                            int port)
            throws ActivationException, RemoteException{
        return exportObject(obj,location,data,restart,port,null,null);
    }

    public static ActivationID exportObject(Remote obj,
                                            String location,
                                            MarshalledObject<?> data,
                                            boolean restart,
                                            int port,
                                            RMIClientSocketFactory csf,
                                            RMIServerSocketFactory ssf)
            throws ActivationException, RemoteException{
        ActivationDesc desc=new ActivationDesc(obj.getClass().getName(),
                location,data,restart);
        /**
         * Register descriptor.
         */
        ActivationSystem system=ActivationGroup.getSystem();
        ActivationID id=system.registerObject(desc);
        /**
         * Export object.
         */
        try{
            exportObject(obj,id,port,csf,ssf);
        }catch(RemoteException e){
            /**
             * Attempt to unregister activation descriptor because export
             * failed and register/export should be atomic (see 4323621).
             */
            try{
                system.unregisterObject(id);
            }catch(Exception ex){
            }
            /**
             * Report original exception.
             */
            throw e;
        }
        /**
         * This call can't fail (it is a local call, and the only possible
         * exception, thrown if the group is inactive, will not be thrown
         * because the group is not inactive).
         */
        ActivationGroup.currentGroup().activeObject(id,obj);
        return id;
    }

    public static Remote exportObject(Remote obj,
                                      ActivationID id,
                                      int port,
                                      RMIClientSocketFactory csf,
                                      RMIServerSocketFactory ssf)
            throws RemoteException{
        return exportObject(obj,new ActivatableServerRef(id,port,csf,ssf));
    }

    private static Remote exportObject(Remote obj,ActivatableServerRef sref)
            throws RemoteException{
        // if obj extends Activatable, set its ref.
        if(obj instanceof Activatable){
            ((Activatable)obj).ref=sref;
        }
        return sref.exportObject(obj,null,false);
    }

    protected Activatable(String location,
                          MarshalledObject<?> data,
                          boolean restart,
                          int port,
                          RMIClientSocketFactory csf,
                          RMIServerSocketFactory ssf)
            throws ActivationException, RemoteException{
        super();
        id=exportObject(this,location,data,restart,port,csf,ssf);
    }

    protected Activatable(ActivationID id,int port)
            throws RemoteException{
        super();
        this.id=id;
        exportObject(this,id,port);
    }

    public static Remote exportObject(Remote obj,
                                      ActivationID id,
                                      int port)
            throws RemoteException{
        return exportObject(obj,new ActivatableServerRef(id,port));
    }

    protected Activatable(ActivationID id,int port,
                          RMIClientSocketFactory csf,
                          RMIServerSocketFactory ssf)
            throws RemoteException{
        super();
        this.id=id;
        exportObject(this,id,port,csf,ssf);
    }

    public static Remote register(ActivationDesc desc)
            throws UnknownGroupException, ActivationException, RemoteException{
        // register object with activator.
        ActivationID id=
                ActivationGroup.getSystem().registerObject(desc);
        return sun.rmi.server.ActivatableRef.getStub(desc,id);
    }

    public static boolean inactive(ActivationID id)
            throws UnknownObjectException, ActivationException, RemoteException{
        return ActivationGroup.currentGroup().inactiveObject(id);
    }

    public static void unregister(ActivationID id)
            throws UnknownObjectException, ActivationException, RemoteException{
        ActivationGroup.getSystem().unregisterObject(id);
    }

    public static boolean unexportObject(Remote obj,boolean force)
            throws NoSuchObjectException{
        return sun.rmi.transport.ObjectTable.unexportObject(obj,force);
    }

    protected ActivationID getID(){
        return id;
    }
}
