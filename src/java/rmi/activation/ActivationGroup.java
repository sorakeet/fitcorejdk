/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.activation;

import sun.security.action.GetIntegerAction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.MarshalledObject;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.UnicastRemoteObject;
import java.security.AccessController;

public abstract class ActivationGroup
        extends UnicastRemoteObject
        implements ActivationInstantiator{
    private static final long serialVersionUID=-7696947875314805420L;
    private static ActivationGroup currGroup;
    private static ActivationGroupID currGroupID;
    private static ActivationSystem currSystem;
    private static boolean canCreate=true;
    private ActivationGroupID groupID;
    private ActivationMonitor monitor;
    private long incarnation;

    protected ActivationGroup(ActivationGroupID groupID)
            throws RemoteException{
        // call super constructor to export the object
        super();
        this.groupID=groupID;
    }

    public static synchronized ActivationGroup createGroup(ActivationGroupID id,
                                                           final ActivationGroupDesc desc,
                                                           long incarnation)
            throws ActivationException{
        SecurityManager security=System.getSecurityManager();
        if(security!=null)
            security.checkSetFactory();
        if(currGroup!=null)
            throw new ActivationException("group already exists");
        if(canCreate==false)
            throw new ActivationException("group deactivated and "+
                    "cannot be recreated");
        try{
            // load group's class
            String groupClassName=desc.getClassName();
            Class<? extends ActivationGroup> cl;
            Class<? extends ActivationGroup> defaultGroupClass=
                    sun.rmi.server.ActivationGroupImpl.class;
            if(groupClassName==null||       // see 4252236
                    groupClassName.equals(defaultGroupClass.getName())){
                cl=defaultGroupClass;
            }else{
                Class<?> cl0;
                try{
                    cl0=RMIClassLoader.loadClass(desc.getLocation(),
                            groupClassName);
                }catch(Exception ex){
                    throw new ActivationException(
                            "Could not load group implementation class",ex);
                }
                if(ActivationGroup.class.isAssignableFrom(cl0)){
                    cl=cl0.asSubclass(ActivationGroup.class);
                }else{
                    throw new ActivationException("group not correct class: "+
                            cl0.getName());
                }
            }
            // create group
            Constructor<? extends ActivationGroup> constructor=
                    cl.getConstructor(ActivationGroupID.class,
                            MarshalledObject.class);
            ActivationGroup newGroup=
                    constructor.newInstance(id,desc.getData());
            currSystem=id.getSystem();
            newGroup.incarnation=incarnation;
            newGroup.monitor=
                    currSystem.activeGroup(id,newGroup,incarnation);
            currGroup=newGroup;
            currGroupID=id;
            canCreate=false;
        }catch(InvocationTargetException e){
            e.getTargetException().printStackTrace();
            throw new ActivationException("exception in group constructor",
                    e.getTargetException());
        }catch(ActivationException e){
            throw e;
        }catch(Exception e){
            throw new ActivationException("exception creating group",e);
        }
        return currGroup;
    }

    public static synchronized ActivationGroupID currentGroupID(){
        return currGroupID;
    }

    static synchronized ActivationGroupID internalCurrentGroupID()
            throws ActivationException{
        if(currGroupID==null)
            throw new ActivationException("nonexistent group");
        return currGroupID;
    }

    public static synchronized ActivationSystem getSystem()
            throws ActivationException{
        if(currSystem==null){
            try{
                int port=AccessController.doPrivileged(
                        new GetIntegerAction("java.rmi.activation.port",
                                ActivationSystem.SYSTEM_PORT));
                currSystem=(ActivationSystem)
                        Naming.lookup("//:"+port+
                                "/java.rmi.activation.ActivationSystem");
            }catch(Exception e){
                throw new ActivationException(
                        "unable to obtain ActivationSystem",e);
            }
        }
        return currSystem;
    }

    public static synchronized void setSystem(ActivationSystem system)
            throws ActivationException{
        SecurityManager security=System.getSecurityManager();
        if(security!=null)
            security.checkSetFactory();
        if(currSystem!=null)
            throw new ActivationException("activation system already set");
        currSystem=system;
    }

    static synchronized ActivationGroup currentGroup()
            throws ActivationException{
        if(currGroup==null){
            throw new ActivationException("group is not active");
        }
        return currGroup;
    }

    public boolean inactiveObject(ActivationID id)
            throws ActivationException, UnknownObjectException, RemoteException{
        getMonitor().inactiveObject(id);
        return true;
    }

    private ActivationMonitor getMonitor() throws RemoteException{
        synchronized(ActivationGroup.class){
            if(monitor!=null){
                return monitor;
            }
        }
        throw new RemoteException("monitor not received");
    }

    public abstract void activeObject(ActivationID id,Remote obj)
            throws ActivationException, UnknownObjectException, RemoteException;

    protected void activeObject(ActivationID id,
                                MarshalledObject<? extends Remote> mobj)
            throws ActivationException, UnknownObjectException, RemoteException{
        getMonitor().activeObject(id,mobj);
    }

    protected void inactiveGroup()
            throws UnknownGroupException, RemoteException{
        try{
            getMonitor().inactiveGroup(groupID,incarnation);
        }finally{
            destroyGroup();
        }
    }

    private static synchronized void destroyGroup(){
        currGroup=null;
        currGroupID=null;
        // NOTE: don't set currSystem to null since it may be needed
    }
}
