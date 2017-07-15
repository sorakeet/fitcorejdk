/**
 * Copyright (c) 1995, 2004, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.corba.se.impl.util;

import com.sun.corba.se.impl.orbutil.GetPropertyAction;

import java.net.MalformedURLException;
import java.rmi.server.RMIClassLoader;
import java.security.AccessController;

public class JDKBridge{
    private static final String LOCAL_CODEBASE_KEY="java.rmi.server.codebase";
    private static final String USE_CODEBASE_ONLY_KEY="java.rmi.server.useCodebaseOnly";
    private static String localCodebase=null;
    private static boolean useCodebaseOnly;

    static{
        setCodebaseProperties();
    }

    public static String getLocalCodebase(){
        return localCodebase;
    }

    public static synchronized void setLocalCodebase(String codebase){
        localCodebase=codebase;
    }

    public static boolean useCodebaseOnly(){
        return useCodebaseOnly;
    }

    public static Class loadClass(String className,
                                  String remoteCodebase)
            throws ClassNotFoundException{
        return loadClass(className,remoteCodebase,null);
    }

    public static Class loadClass(String className,
                                  String remoteCodebase,
                                  ClassLoader loader)
            throws ClassNotFoundException{
        if(loader==null){
            return loadClassM(className,remoteCodebase,useCodebaseOnly);
        }else{
            try{
                return loadClassM(className,remoteCodebase,useCodebaseOnly);
            }catch(ClassNotFoundException e){
                return loader.loadClass(className);
            }
        }
    }

    private static Class loadClassM(String className,
                                    String remoteCodebase,
                                    boolean useCodebaseOnly)
            throws ClassNotFoundException{
        try{
            return JDKClassLoader.loadClass(null,className);
        }catch(ClassNotFoundException e){
        }
        try{
            if(!useCodebaseOnly&&remoteCodebase!=null){
                return RMIClassLoader.loadClass(remoteCodebase,
                        className);
            }else{
                return RMIClassLoader.loadClass(className);
            }
        }catch(MalformedURLException e){
            className=className+": "+e.toString();
        }
        throw new ClassNotFoundException(className);
    }

    public static Class loadClass(String className)
            throws ClassNotFoundException{
        return loadClass(className,null,null);
    }

    public static final void main(String[] args){
        System.out.println("1.2 VM");
        /**
         // If on 1.2, use a policy with all permissions.
         System.setSecurityManager (new javax.rmi.download.SecurityManager());
         String targetClass = "[[Lrmic.Typedef;";
         System.out.println("localCodebase =  "+localCodebase);
         System.out.println("Trying to load "+targetClass);
         try {
         Class clz = loadClass(targetClass,null,localCodebase);
         System.out.println("Loaded: "+clz);
         } catch (ClassNotFoundException e) {
         System.out.println("Caught "+e);
         }
         */
    }

    public static synchronized void setCodebaseProperties(){
        String prop=(String)AccessController.doPrivileged(
                new GetPropertyAction(LOCAL_CODEBASE_KEY)
        );
        if(prop!=null&&prop.trim().length()>0){
            localCodebase=prop;
        }
        prop=(String)AccessController.doPrivileged(
                new GetPropertyAction(USE_CODEBASE_ONLY_KEY)
        );
        if(prop!=null&&prop.trim().length()>0){
            useCodebaseOnly=Boolean.valueOf(prop).booleanValue();
        }
    }
}
