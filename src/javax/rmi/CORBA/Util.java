/**
 * Copyright (c) 1998, 2016, Oracle and/or its affiliates. All rights reserved.
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
package javax.rmi.CORBA;

import com.sun.corba.se.impl.orbutil.GetPropertyAction;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

import java.io.SerializablePermission;
import java.net.MalformedURLException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

public class Util{
    // This can only be set at static initialization time (no sync necessary).
    private static final UtilDelegate utilDelegate;
    private static final String UtilClassKey="javax.rmi.CORBA.UtilClass";
    private static final String ALLOW_CREATEVALUEHANDLER_PROP="jdk.rmi.CORBA.allowCustomValueHandler";
    private static boolean allowCustomValueHandler;

    static{
        utilDelegate=(UtilDelegate)createDelegate(UtilClassKey);
        allowCustomValueHandler=readAllowCustomValueHandlerProperty();
    }

    private Util(){
    }

    private static boolean readAllowCustomValueHandlerProperty(){
        return AccessController
                .doPrivileged(new PrivilegedAction<Boolean>(){
                    @Override
                    public Boolean run(){
                        return Boolean.getBoolean(ALLOW_CREATEVALUEHANDLER_PROP);
                    }
                });
    }

    public static RemoteException mapSystemException(SystemException ex){
        if(utilDelegate!=null){
            return utilDelegate.mapSystemException(ex);
        }
        return null;
    }

    public static void writeAny(OutputStream out,Object obj){
        if(utilDelegate!=null){
            utilDelegate.writeAny(out,obj);
        }
    }

    public static Object readAny(InputStream in){
        if(utilDelegate!=null){
            return utilDelegate.readAny(in);
        }
        return null;
    }

    public static void writeRemoteObject(OutputStream out,
                                         Object obj){
        if(utilDelegate!=null){
            utilDelegate.writeRemoteObject(out,obj);
        }
    }

    public static void writeAbstractObject(OutputStream out,
                                           Object obj){
        if(utilDelegate!=null){
            utilDelegate.writeAbstractObject(out,obj);
        }
    }

    public static void registerTarget(Tie tie,
                                      Remote target){
        if(utilDelegate!=null){
            utilDelegate.registerTarget(tie,target);
        }
    }

    public static void unexportObject(Remote target)
            throws java.rmi.NoSuchObjectException{
        if(utilDelegate!=null){
            utilDelegate.unexportObject(target);
        }
    }

    public static Tie getTie(Remote target){
        if(utilDelegate!=null){
            return utilDelegate.getTie(target);
        }
        return null;
    }

    public static ValueHandler createValueHandler(){
        isCustomSerializationPermitted();
        if(utilDelegate!=null){
            return utilDelegate.createValueHandler();
        }
        return null;
    }

    private static void isCustomSerializationPermitted(){
        SecurityManager sm=System.getSecurityManager();
        if(!allowCustomValueHandler){
            if(sm!=null){
                // check that a serialization permission has been
                // set to allow the loading of the Util delegate
                // which provides access to custom ValueHandler
                sm.checkPermission(new SerializablePermission(
                        "enableCustomValueHandler"));
            }
        }
    }

    public static String getCodebase(Class clz){
        if(utilDelegate!=null){
            return utilDelegate.getCodebase(clz);
        }
        return null;
    }

    public static Class loadClass(String className,
                                  String remoteCodebase,
                                  ClassLoader loader)
            throws ClassNotFoundException{
        if(utilDelegate!=null){
            return utilDelegate.loadClass(className,remoteCodebase,loader);
        }
        return null;
    }

    public static boolean isLocal(Stub stub) throws RemoteException{
        if(utilDelegate!=null){
            return utilDelegate.isLocal(stub);
        }
        return false;
    }

    public static RemoteException wrapException(Throwable orig){
        if(utilDelegate!=null){
            return utilDelegate.wrapException(orig);
        }
        return null;
    }

    public static Object[] copyObjects(Object[] obj,ORB orb)
            throws RemoteException{
        if(utilDelegate!=null){
            return utilDelegate.copyObjects(obj,orb);
        }
        return null;
    }

    public static Object copyObject(Object obj,ORB orb)
            throws RemoteException{
        if(utilDelegate!=null){
            return utilDelegate.copyObject(obj,orb);
        }
        return null;
    }

    // Same code as in PortableRemoteObject. Can not be shared because they
    // are in different packages and the visibility needs to be package for
    // security reasons. If you know a better solution how to share this code
    // then remove it from PortableRemoteObject. Also in Stub.java
    private static Object createDelegate(String classKey){
        String className=(String)
                AccessController.doPrivileged(new GetPropertyAction(classKey));
        if(className==null){
            Properties props=getORBPropertiesFile();
            if(props!=null){
                className=props.getProperty(classKey);
            }
        }
        if(className==null){
            return new com.sun.corba.se.impl.javax.rmi.CORBA.Util();
        }
        try{
            return loadDelegateClass(className).newInstance();
        }catch(ClassNotFoundException ex){
            INITIALIZE exc=new INITIALIZE("Cannot instantiate "+className);
            exc.initCause(ex);
            throw exc;
        }catch(Exception ex){
            INITIALIZE exc=new INITIALIZE("Error while instantiating"+className);
            exc.initCause(ex);
            throw exc;
        }
    }

    private static Class loadDelegateClass(String className) throws ClassNotFoundException{
        try{
            ClassLoader loader=Thread.currentThread().getContextClassLoader();
            return Class.forName(className,false,loader);
        }catch(ClassNotFoundException e){
            // ignore, then try RMIClassLoader
        }
        try{
            return RMIClassLoader.loadClass(className);
        }catch(MalformedURLException e){
            String msg="Could not load "+className+": "+e.toString();
            ClassNotFoundException exc=new ClassNotFoundException(msg);
            throw exc;
        }
    }

    private static Properties getORBPropertiesFile(){
        return (Properties)AccessController.doPrivileged(
                new GetORBPropertiesFileAction());
    }
}
