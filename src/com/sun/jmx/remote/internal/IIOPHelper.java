/**
 * Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.remote.internal;

import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

public final class IIOPHelper{
    // loads IIOPProxy implementation class if available
    private static final String IMPL_CLASS=
            "com.sun.jmx.remote.protocol.iiop.IIOPProxyImpl";
    private static final IIOPProxy proxy=
            AccessController.doPrivileged(new PrivilegedAction<IIOPProxy>(){
                public IIOPProxy run(){
                    try{
                        Class<?> c=Class.forName(IMPL_CLASS,true,
                                IIOPHelper.class.getClassLoader());
                        return (IIOPProxy)c.newInstance();
                    }catch(ClassNotFoundException cnf){
                        return null;
                    }catch(InstantiationException e){
                        throw new AssertionError(e);
                    }catch(IllegalAccessException e){
                        throw new AssertionError(e);
                    }
                }
            });
    private IIOPHelper(){
    }

    public static boolean isAvailable(){
        return proxy!=null;
    }

    public static boolean isStub(Object obj){
        return (proxy==null)?false:proxy.isStub(obj);
    }

    public static Object getDelegate(Object stub){
        ensureAvailable();
        return proxy.getDelegate(stub);
    }

    private static void ensureAvailable(){
        if(proxy==null)
            throw new AssertionError("Should not here");
    }

    public static void setDelegate(Object stub,Object delegate){
        ensureAvailable();
        proxy.setDelegate(stub,delegate);
    }

    public static Object getOrb(Object stub){
        ensureAvailable();
        return proxy.getOrb(stub);
    }

    public static void connect(Object stub,Object orb)
            throws IOException{
        if(proxy==null)
            throw new IOException("Connection to ORB failed, RMI/IIOP not available");
        proxy.connect(stub,orb);
    }

    public static boolean isOrb(Object obj){
        return (proxy==null)?false:proxy.isOrb(obj);
    }

    public static Object createOrb(String[] args,Properties props)
            throws IOException{
        if(proxy==null)
            throw new IOException("ORB initialization failed, RMI/IIOP not available");
        return proxy.createOrb(args,props);
    }

    public static Object stringToObject(Object orb,String str){
        ensureAvailable();
        return proxy.stringToObject(orb,str);
    }

    public static String objectToString(Object orb,Object obj){
        ensureAvailable();
        return proxy.objectToString(orb,obj);
    }

    public static <T> T narrow(Object narrowFrom,Class<T> narrowTo){
        ensureAvailable();
        return proxy.narrow(narrowFrom,narrowTo);
    }

    public static void exportObject(Remote obj) throws IOException{
        if(proxy==null)
            throw new IOException("RMI object cannot be exported, RMI/IIOP not available");
        proxy.exportObject(obj);
    }

    public static void unexportObject(Remote obj) throws IOException{
        if(proxy==null)
            throw new NoSuchObjectException("Object not exported");
        proxy.unexportObject(obj);
    }

    public static Remote toStub(Remote obj) throws IOException{
        if(proxy==null)
            throw new NoSuchObjectException("Object not exported");
        return proxy.toStub(obj);
    }
}
