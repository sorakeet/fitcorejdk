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
package com.sun.corba.se.impl.javax.rmi.CORBA; // Util (sed marker, don't remove!)

import com.sun.corba.se.impl.io.ValueHandlerImpl;
import com.sun.corba.se.impl.logging.OMGSystemException;
import com.sun.corba.se.impl.logging.UtilSystemException;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.util.IdentityHashtable;
import com.sun.corba.se.impl.util.JDKBridge;
import com.sun.corba.se.impl.util.Utility;
import com.sun.corba.se.pept.transport.ContactInfoList;
import com.sun.corba.se.spi.copyobject.CopierManager;
import com.sun.corba.se.spi.copyobject.ObjectCopier;
import com.sun.corba.se.spi.copyobject.ReflectiveCopyException;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orb.ORBVersionFactory;
import com.sun.corba.se.spi.protocol.CorbaClientDelegate;
import com.sun.corba.se.spi.protocol.LocalClientRequestDispatcher;
import com.sun.corba.se.spi.transport.CorbaContactInfoList;
import org.omg.CORBA.*;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.UnknownException;
import sun.corba.SharedSecrets;

import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.ValueHandler;
import javax.transaction.InvalidTransactionException;
import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.Object;
import java.lang.reflect.Constructor;
import java.rmi.*;
import java.rmi.server.RMIClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
// This class must be able to function with non-Sun ORBs.
// This means that any of the following com.sun.corba classes
// must only occur in contexts that also handle the non-Sun case.

public class Util implements javax.rmi.CORBA.UtilDelegate{
    private static final ValueHandlerImpl valueHandlerSingleton=
            SharedSecrets.getJavaCorbaAccess().newValueHandlerImpl();
    // Runs as long as there are exportedServants
    private static KeepAlive keepAlive=null;
    // Maps targets to ties.
    private static IdentityHashtable exportedServants=new IdentityHashtable();
    private static Util instance=null;
    private UtilSystemException utilWrapper=UtilSystemException.get(
            CORBALogDomains.RPC_ENCODING);

    public Util(){
        setInstance(this);
    }

    public static Util getInstance(){
        return instance;
    }

    private static void setInstance(Util util){
        assert instance==null:"Instance already defined";
        instance=util;
    }

    public static boolean isInstanceDefined(){
        return instance!=null;
    }

    // Used by TOAFactory.shutdown to unexport all targets for this
    // particular ORB.  This happens during ORB shutdown.
    public void unregisterTargetsForORB(org.omg.CORBA.ORB orb){
        for(Enumeration e=exportedServants.keys();e.hasMoreElements();){
            Object key=e.nextElement();
            Remote target=(Remote)(key instanceof Tie?((Tie)key).getTarget():key);
            // Bug 4476347: BAD_OPERATION is thrown if the ties delegate isn't set.
            // We can ignore this because it means the tie is not connected to an ORB.
            try{
                if(orb==getTie(target).orb()){
                    try{
                        unexportObject(target);
                    }catch(NoSuchObjectException ex){
                        // We neglect this exception if at all if it is
                        // raised. It is not harmful.
                    }
                }
            }catch(BAD_OPERATION bad){
                /** Ignore */
            }
        }
    }

    public RemoteException mapSystemException(SystemException ex){
        if(ex instanceof UnknownException){
            Throwable orig=((UnknownException)ex).originalEx;
            if(orig instanceof Error){
                return new ServerError("Error occurred in server thread",(Error)orig);
            }else if(orig instanceof RemoteException){
                return new ServerException("RemoteException occurred in server thread",
                        (Exception)orig);
            }else if(orig instanceof RuntimeException){
                throw (RuntimeException)orig;
            }
        }
        // Build the message string...
        String name=ex.getClass().getName();
        String corbaName=name.substring(name.lastIndexOf('.')+1);
        String status;
        switch(ex.completed.value()){
            case CompletionStatus._COMPLETED_YES:
                status="Yes";
                break;
            case CompletionStatus._COMPLETED_NO:
                status="No";
                break;
            case CompletionStatus._COMPLETED_MAYBE:
            default:
                status="Maybe";
                break;
        }
        String message="CORBA "+corbaName+" "+ex.minor+" "+status;
        // Now map to the correct RemoteException type...
        if(ex instanceof COMM_FAILURE){
            return new MarshalException(message,ex);
        }else if(ex instanceof INV_OBJREF){
            RemoteException newEx=new NoSuchObjectException(message);
            newEx.detail=ex;
            return newEx;
        }else if(ex instanceof NO_PERMISSION){
            return new AccessException(message,ex);
        }else if(ex instanceof MARSHAL){
            return new MarshalException(message,ex);
        }else if(ex instanceof OBJECT_NOT_EXIST){
            RemoteException newEx=new NoSuchObjectException(message);
            newEx.detail=ex;
            return newEx;
        }else if(ex instanceof TRANSACTION_REQUIRED){
            RemoteException newEx=new TransactionRequiredException(message);
            newEx.detail=ex;
            return newEx;
        }else if(ex instanceof TRANSACTION_ROLLEDBACK){
            RemoteException newEx=new TransactionRolledbackException(message);
            newEx.detail=ex;
            return newEx;
        }else if(ex instanceof INVALID_TRANSACTION){
            RemoteException newEx=new InvalidTransactionException(message);
            newEx.detail=ex;
            return newEx;
        }else if(ex instanceof BAD_PARAM){
            Exception inner=ex;
            // Pre-Merlin Sun ORBs used the incorrect minor code for
            // this case.  See Java to IDL ptc-00-01-08 1.4.8.
            if(ex.minor==ORBConstants.LEGACY_SUN_NOT_SERIALIZABLE||
                    ex.minor==OMGSystemException.NOT_SERIALIZABLE){
                if(ex.getMessage()!=null)
                    inner=new NotSerializableException(ex.getMessage());
                else
                    inner=new NotSerializableException();
                inner.initCause(ex);
            }
            return new MarshalException(message,inner);
        }else if(ex instanceof ACTIVITY_REQUIRED){
            try{
                Class<?> cl=SharedSecrets.getJavaCorbaAccess().loadClass(
                        "javax.activity.ActivityRequiredException");
                Class[] params=new Class[2];
                params[0]=String.class;
                params[1]=Throwable.class;
                Constructor cr=cl.getConstructor(params);
                Object[] args=new Object[2];
                args[0]=message;
                args[1]=ex;
                return (RemoteException)cr.newInstance(args);
            }catch(Throwable e){
                utilWrapper.classNotFound(
                        e,"javax.activity.ActivityRequiredException");
            }
        }else if(ex instanceof ACTIVITY_COMPLETED){
            try{
                Class<?> cl=SharedSecrets.getJavaCorbaAccess().loadClass(
                        "javax.activity.ActivityCompletedException");
                Class[] params=new Class[2];
                params[0]=String.class;
                params[1]=Throwable.class;
                Constructor cr=cl.getConstructor(params);
                Object[] args=new Object[2];
                args[0]=message;
                args[1]=ex;
                return (RemoteException)cr.newInstance(args);
            }catch(Throwable e){
                utilWrapper.classNotFound(
                        e,"javax.activity.ActivityCompletedException");
            }
        }else if(ex instanceof INVALID_ACTIVITY){
            try{
                Class<?> cl=SharedSecrets.getJavaCorbaAccess().loadClass(
                        "javax.activity.InvalidActivityException");
                Class[] params=new Class[2];
                params[0]=String.class;
                params[1]=Throwable.class;
                Constructor cr=cl.getConstructor(params);
                Object[] args=new Object[2];
                args[0]=message;
                args[1]=ex;
                return (RemoteException)cr.newInstance(args);
            }catch(Throwable e){
                utilWrapper.classNotFound(
                        e,"javax.activity.InvalidActivityException");
            }
        }
        // Just map to a generic RemoteException...
        return new RemoteException(message,ex);
    }

    public void writeAny(OutputStream out,
                         Object obj){
        org.omg.CORBA.ORB orb=out.orb();
        // Create Any
        Any any=orb.create_any();
        // Make sure we have a connected object...
        Object newObj=Utility.autoConnect(obj,orb,false);
        if(newObj instanceof org.omg.CORBA.Object){
            any.insert_Object((org.omg.CORBA.Object)newObj);
        }else{
            if(newObj==null){
                // Handle the null case, including backwards
                // compatibility issues
                any.insert_Value(null,createTypeCodeForNull(orb));
            }else{
                if(newObj instanceof Serializable){
                    // If they're our Any and ORB implementations,
                    // we may want to do type code related versioning.
                    TypeCode tc=createTypeCode((Serializable)newObj,any,orb);
                    if(tc==null)
                        any.insert_Value((Serializable)newObj);
                    else
                        any.insert_Value((Serializable)newObj,tc);
                }else if(newObj instanceof Remote){
                    ORBUtility.throwNotSerializableForCorba(newObj.getClass().getName());
                }else{
                    ORBUtility.throwNotSerializableForCorba(newObj.getClass().getName());
                }
            }
        }
        out.write_any(any);
    }

    private TypeCode createTypeCode(Serializable obj,
                                    Any any,
                                    org.omg.CORBA.ORB orb){
        if(any instanceof com.sun.corba.se.impl.corba.AnyImpl&&
                orb instanceof ORB){
            com.sun.corba.se.impl.corba.AnyImpl anyImpl
                    =(com.sun.corba.se.impl.corba.AnyImpl)any;
            ORB ourORB=(ORB)orb;
            return anyImpl.createTypeCodeForClass(obj.getClass(),ourORB);
        }else
            return null;
    }

    private TypeCode createTypeCodeForNull(org.omg.CORBA.ORB orb){
        if(orb instanceof ORB){
            ORB ourORB=(ORB)orb;
            // Preserve backwards compatibility with Kestrel and Ladybird
            // by not fully implementing interop issue resolution 3857,
            // and returning a null TypeCode with a tk_value TCKind.
            // If we're not talking to Kestrel or Ladybird, fall through
            // to the abstract interface case (also used for foreign ORBs).
            if(!ORBVersionFactory.getFOREIGN().equals(ourORB.getORBVersion())&&
                    ORBVersionFactory.getNEWER().compareTo(ourORB.getORBVersion())>0){
                return orb.get_primitive_tc(TCKind.tk_value);
            }
        }
        // Use tk_abstract_interface as detailed in the resolution
        // REVISIT: Define this in IDL and get the ID in generated code
        String abstractBaseID="IDL:omg.org/CORBA/AbstractBase:1.0";
        return orb.create_abstract_interface_tc(abstractBaseID,"");
    }

    public Object readAny(InputStream in){
        Any any=in.read_any();
        if(any.type().kind().value()==TCKind._tk_objref)
            return any.extract_Object();
        else
            return any.extract_Value();
    }

    public void writeRemoteObject(OutputStream out,Object obj){
        // Make sure we have a connected object, then
        // write it out...
        Object newObj=Utility.autoConnect(obj,out.orb(),false);
        out.write_Object((org.omg.CORBA.Object)newObj);
    }

    public void writeAbstractObject(OutputStream out,Object obj){
        // Make sure we have a connected object, then
        // write it out...
        Object newObj=Utility.autoConnect(obj,out.orb(),false);
        ((org.omg.CORBA_2_3.portable.OutputStream)out).write_abstract_interface(newObj);
    }

    public void registerTarget(Tie tie,Remote target){
        synchronized(exportedServants){
            // Do we already have this target registered?
            if(lookupTie(target)==null){
                // No, so register it and set the target...
                exportedServants.put(target,tie);
                tie.setTarget(target);
                // Do we need to instantiate our keep-alive thread?
                if(keepAlive==null){
                    // Yes. Instantiate our keep-alive thread and start
                    // it up...
                    keepAlive=(KeepAlive)AccessController.doPrivileged(new PrivilegedAction(){
                        public Object run(){
                            return new KeepAlive();
                        }
                    });
                    keepAlive.start();
                }
            }
        }
    }

    public void unexportObject(Remote target)
            throws NoSuchObjectException{
        synchronized(exportedServants){
            Tie cachedTie=lookupTie(target);
            if(cachedTie!=null){
                exportedServants.remove(target);
                Utility.purgeStubForTie(cachedTie);
                Utility.purgeTieAndServant(cachedTie);
                try{
                    cleanUpTie(cachedTie);
                }catch(BAD_OPERATION e){
                    // ignore
                }catch(org.omg.CORBA.OBJ_ADAPTER e){
                    // This can happen when the target was never associated with a POA.
                    // We can safely ignore this case.
                }
                // Is it time to shut down our keep alive thread?
                if(exportedServants.isEmpty()){
                    keepAlive.quit();
                    keepAlive=null;
                }
            }else{
                throw new NoSuchObjectException("Tie not found");
            }
        }
    }

    protected void cleanUpTie(Tie cachedTie)
            throws NoSuchObjectException{
        cachedTie.setTarget(null);
        cachedTie.deactivate();
    }

    public Tie getTie(Remote target){
        synchronized(exportedServants){
            return lookupTie(target);
        }
    }

    private static Tie lookupTie(Remote target){
        Tie result=(Tie)exportedServants.get(target);
        if(result==null&&target instanceof Tie){
            if(exportedServants.contains(target)){
                result=(Tie)target;
            }
        }
        return result;
    }

    public ValueHandler createValueHandler(){
        return valueHandlerSingleton;
    }

    public String getCodebase(Class clz){
        return RMIClassLoader.getClassAnnotation(clz);
    }

    public Class loadClass(String className,String remoteCodebase,
                           ClassLoader loader) throws ClassNotFoundException{
        return JDKBridge.loadClass(className,remoteCodebase,loader);
    }

    public boolean isLocal(javax.rmi.CORBA.Stub stub) throws RemoteException{
        boolean result=false;
        try{
            org.omg.CORBA.portable.Delegate delegate=stub._get_delegate();
            if(delegate instanceof CorbaClientDelegate){
                // For the Sun ORB
                CorbaClientDelegate cdel=(CorbaClientDelegate)delegate;
                ContactInfoList cil=cdel.getContactInfoList();
                if(cil instanceof CorbaContactInfoList){
                    CorbaContactInfoList ccil=(CorbaContactInfoList)cil;
                    LocalClientRequestDispatcher lcs=ccil.getLocalClientRequestDispatcher();
                    result=lcs.useLocalInvocation(null);
                }
            }else{
                // For a non-Sun ORB
                result=delegate.is_local(stub);
            }
        }catch(SystemException e){
            throw javax.rmi.CORBA.Util.mapSystemException(e);
        }
        return result;
    }

    public RemoteException wrapException(Throwable orig){
        if(orig instanceof SystemException){
            return mapSystemException((SystemException)orig);
        }
        if(orig instanceof Error){
            return new ServerError("Error occurred in server thread",(Error)orig);
        }else if(orig instanceof RemoteException){
            return new ServerException("RemoteException occurred in server thread",
                    (Exception)orig);
        }else if(orig instanceof RuntimeException){
            throw (RuntimeException)orig;
        }
        if(orig instanceof Exception)
            return new UnexpectedException(orig.toString(),(Exception)orig);
        else
            return new UnexpectedException(orig.toString());
    }

    public Object[] copyObjects(Object[] obj,org.omg.CORBA.ORB orb)
            throws RemoteException{
        if(obj==null)
            // Bug fix for 5018613: JCK test expects copyObjects to throw
            // NPE when obj==null.  This is actually not in the spec, since
            // obj is not really an RMI-IDL data type, but we follow our
            // test here, and force this error to be thrown.
            throw new NullPointerException();
        Class compType=obj.getClass().getComponentType();
        if(Remote.class.isAssignableFrom(compType)&&!compType.isInterface()){
            // obj is an array of remote impl types.  This
            // causes problems with stream copier, so we copy
            // it over to an array of Remotes instead.
            Remote[] result=new Remote[obj.length];
            System.arraycopy((Object)obj,0,(Object)result,0,obj.length);
            return (Object[])copyObject(result,orb);
        }else
            return (Object[])copyObject(obj,orb);
    }

    public Object copyObject(Object obj,org.omg.CORBA.ORB orb)
            throws RemoteException{
        if(orb instanceof ORB){
            ORB lorb=(ORB)orb;
            try{
                try{
                    // This gets the copier for the current invocation, which was
                    // previously set by preinvoke.
                    return lorb.peekInvocationInfo().getCopierFactory().make().copy(obj);
                }catch(java.util.EmptyStackException exc){
                    // copyObject was invoked outside of an invocation, probably by
                    // a test.  Get the default copier from the ORB.
                    // XXX should we just make the default copier available directly
                    // and avoid constructing one on each call?
                    CopierManager cm=lorb.getCopierManager();
                    ObjectCopier copier=cm.getDefaultObjectCopierFactory().make();
                    return copier.copy(obj);
                }
            }catch(ReflectiveCopyException exc){
                RemoteException rexc=new RemoteException();
                rexc.initCause(exc);
                throw rexc;
            }
        }else{
            org.omg.CORBA_2_3.portable.OutputStream out=
                    (org.omg.CORBA_2_3.portable.OutputStream)orb.create_output_stream();
            out.write_value((Serializable)obj);
            org.omg.CORBA_2_3.portable.InputStream in=
                    (org.omg.CORBA_2_3.portable.InputStream)out.create_input_stream();
            return in.read_value();
        }
    }
}

class KeepAlive extends Thread{
    boolean quit=false;

    public KeepAlive(){
        setDaemon(false);
    }

    public synchronized void run(){
        while(!quit){
            try{
                wait();
            }catch(InterruptedException e){
            }
        }
    }

    public synchronized void quit(){
        quit=true;
        notifyAll();
    }
}
