/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.mbeanserver;

import sun.reflect.misc.ConstructorUtil;
import sun.reflect.misc.ReflectUtil;

import javax.management.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.*;
import java.util.Map;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.MBEANSERVER_LOGGER;

public class MBeanInstantiator{
    private static final Map<String,Class<?>> primitiveClasses=Util.newMap();
    //    private MetaData meta = null;

    static{
        for(Class<?> c : new Class<?>[]{byte.class,short.class,int.class,
                long.class,float.class,double.class,
                char.class,boolean.class})
            primitiveClasses.put(c.getName(),c);
    }

    private final ModifiableClassLoaderRepository clr;

    MBeanInstantiator(ModifiableClassLoaderRepository clr){
        this.clr=clr;
    }

    static Class<?>[] loadSignatureClasses(String signature[],
                                           ClassLoader loader)
            throws ReflectionException{
        if(signature==null) return null;
        final ClassLoader aLoader=
                (loader==null?MBeanInstantiator.class.getClassLoader():loader);
        final int length=signature.length;
        final Class<?> tab[]=new Class<?>[length];
        if(length==0) return tab;
        try{
            for(int i=0;i<length;i++){
                // Start handling primitive types (int. boolean and so
                // forth)
                //
                final Class<?> primCla=primitiveClasses.get(signature[i]);
                if(primCla!=null){
                    tab[i]=primCla;
                    continue;
                }
                // Ok we do not have a primitive type ! We need to build
                // the signature of the method
                //
                // We need to load the class through the class
                // loader of the target object.
                //
                ReflectUtil.checkPackageAccess(signature[i]);
                tab[i]=Class.forName(signature[i],false,aLoader);
            }
        }catch(ClassNotFoundException e){
            if(MBEANSERVER_LOGGER.isLoggable(Level.FINEST)){
                MBEANSERVER_LOGGER.logp(Level.FINEST,
                        MBeanInstantiator.class.getName(),
                        "findSignatureClasses",
                        "The parameter class could not be found",e);
            }
            throw new ReflectionException(e,
                    "The parameter class could not be found");
        }catch(RuntimeException e){
            if(MBEANSERVER_LOGGER.isLoggable(Level.FINEST)){
                MBEANSERVER_LOGGER.logp(Level.FINEST,
                        MBeanInstantiator.class.getName(),
                        "findSignatureClasses",
                        "Unexpected exception",e);
            }
            throw e;
        }
        return tab;
    }

    public void testCreation(Class<?> c) throws NotCompliantMBeanException{
        Introspector.testCreation(c);
    }

    public Class<?> findClass(String className,ObjectName aLoader)
            throws ReflectionException, InstanceNotFoundException{
        if(aLoader==null)
            throw new RuntimeOperationsException(new
                    IllegalArgumentException(),"Null loader passed in parameter");
        // Retrieve the class loader from the repository
        ClassLoader loader=null;
        synchronized(this){
            loader=getClassLoader(aLoader);
        }
        if(loader==null){
            throw new InstanceNotFoundException("The loader named "+
                    aLoader+" is not registered in the MBeanServer");
        }
        return findClass(className,loader);
    }

    public Object instantiate(Class<?> theClass)
            throws ReflectionException, MBeanException{
        checkMBeanPermission(theClass,null,null,"instantiate");
        Object moi;
        // ------------------------------
        // ------------------------------
        Constructor<?> cons=findConstructor(theClass,null);
        if(cons==null){
            throw new ReflectionException(new
                    NoSuchMethodException("No such constructor"));
        }
        // Instantiate the new object
        try{
            ReflectUtil.checkPackageAccess(theClass);
            ensureClassAccess(theClass);
            moi=cons.newInstance();
        }catch(InvocationTargetException e){
            // Wrap the exception.
            Throwable t=e.getTargetException();
            if(t instanceof RuntimeException){
                throw new RuntimeMBeanException((RuntimeException)t,
                        "RuntimeException thrown in the MBean's empty constructor");
            }else if(t instanceof Error){
                throw new RuntimeErrorException((Error)t,
                        "Error thrown in the MBean's empty constructor");
            }else{
                throw new MBeanException((Exception)t,
                        "Exception thrown in the MBean's empty constructor");
            }
        }catch(NoSuchMethodError error){
            throw new ReflectionException(new
                    NoSuchMethodException("No constructor"),
                    "No such constructor");
        }catch(InstantiationException e){
            throw new ReflectionException(e,
                    "Exception thrown trying to invoke the MBean's empty constructor");
        }catch(IllegalAccessException e){
            throw new ReflectionException(e,
                    "Exception thrown trying to invoke the MBean's empty constructor");
        }catch(IllegalArgumentException e){
            throw new ReflectionException(e,
                    "Exception thrown trying to invoke the MBean's empty constructor");
        }
        return moi;
    }

    private Constructor<?> findConstructor(Class<?> c,Class<?>[] params){
        try{
            return ConstructorUtil.getConstructor(c,params);
        }catch(Exception e){
            return null;
        }
    }

    private static void checkMBeanPermission(Class<?> clazz,
                                             String member,
                                             ObjectName objectName,
                                             String actions){
        if(clazz!=null){
            checkMBeanPermission(clazz.getName(),member,objectName,actions);
        }
    }

    private static void checkMBeanPermission(String classname,
                                             String member,
                                             ObjectName objectName,
                                             String actions)
            throws SecurityException{
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            Permission perm=new MBeanPermission(classname,
                    member,
                    objectName,
                    actions);
            sm.checkPermission(perm);
        }
    }

    private static void ensureClassAccess(Class clazz)
            throws IllegalAccessException{
        int mod=clazz.getModifiers();
        if(!Modifier.isPublic(mod)){
            throw new IllegalAccessException("Class is not public and can't be instantiated");
        }
    }

    public ObjectInputStream deserialize(ClassLoader loader,byte[] data)
            throws OperationsException{
        // Check parameter validity
        if(data==null){
            throw new RuntimeOperationsException(new
                    IllegalArgumentException(),"Null data passed in parameter");
        }
        if(data.length==0){
            throw new RuntimeOperationsException(new
                    IllegalArgumentException(),"Empty data passed in parameter");
        }
        // Object deserialization
        ByteArrayInputStream bIn;
        ObjectInputStream objIn;
        bIn=new ByteArrayInputStream(data);
        try{
            objIn=new ObjectInputStreamWithLoader(bIn,loader);
        }catch(IOException e){
            throw new OperationsException(
                    "An IOException occurred trying to de-serialize the data");
        }
        return objIn;
    }

    public ObjectInputStream deserialize(String className,
                                         ObjectName loaderName,
                                         byte[] data,
                                         ClassLoader loader)
            throws InstanceNotFoundException,
            OperationsException,
            ReflectionException{
        // Check parameter validity
        if(data==null){
            throw new RuntimeOperationsException(new
                    IllegalArgumentException(),"Null data passed in parameter");
        }
        if(data.length==0){
            throw new RuntimeOperationsException(new
                    IllegalArgumentException(),"Empty data passed in parameter");
        }
        if(className==null){
            throw new RuntimeOperationsException(new
                    IllegalArgumentException(),"Null className passed in parameter");
        }
        ReflectUtil.checkPackageAccess(className);
        Class<?> theClass;
        if(loaderName==null){
            // Load the class using the agent class loader
            theClass=findClass(className,loader);
        }else{
            // Get the class loader MBean
            try{
                ClassLoader instance=null;
                instance=getClassLoader(loaderName);
                if(instance==null)
                    throw new ClassNotFoundException(className);
                theClass=Class.forName(className,false,instance);
            }catch(ClassNotFoundException e){
                throw new ReflectionException(e,
                        "The MBean class could not be loaded by the "+
                                loaderName.toString()+" class loader");
            }
        }
        // Object deserialization
        ByteArrayInputStream bIn;
        ObjectInputStream objIn;
        bIn=new ByteArrayInputStream(data);
        try{
            objIn=new ObjectInputStreamWithLoader(bIn,
                    theClass.getClassLoader());
        }catch(IOException e){
            throw new OperationsException(
                    "An IOException occurred trying to de-serialize the data");
        }
        return objIn;
    }

    public Class<?> findClass(String className,ClassLoader loader)
            throws ReflectionException{
        return loadClass(className,loader);
    }

    static Class<?> loadClass(String className,ClassLoader loader)
            throws ReflectionException{
        Class<?> theClass;
        if(className==null){
            throw new RuntimeOperationsException(new
                    IllegalArgumentException("The class name cannot be null"),
                    "Exception occurred during object instantiation");
        }
        ReflectUtil.checkPackageAccess(className);
        try{
            if(loader==null)
                loader=MBeanInstantiator.class.getClassLoader();
            if(loader!=null){
                theClass=Class.forName(className,false,loader);
            }else{
                theClass=Class.forName(className);
            }
        }catch(ClassNotFoundException e){
            throw new ReflectionException(e,
                    "The MBean class could not be loaded");
        }
        return theClass;
    }

    private ClassLoader getClassLoader(final ObjectName name){
        if(clr==null){
            return null;
        }
        // Restrict to getClassLoader permission only
        Permissions permissions=new Permissions();
        permissions.add(new MBeanPermission("*",null,name,"getClassLoader"));
        ProtectionDomain protectionDomain=new ProtectionDomain(null,permissions);
        ProtectionDomain[] domains={protectionDomain};
        AccessControlContext ctx=new AccessControlContext(domains);
        ClassLoader loader=AccessController.doPrivileged(new PrivilegedAction<ClassLoader>(){
            public ClassLoader run(){
                return clr.getClassLoader(name);
            }
        },ctx);
        return loader;
    }

    public Object instantiate(String className)
            throws ReflectionException,
            MBeanException{
        return instantiate(className,(Object[])null,(String[])null,null);
    }

    public Object instantiate(String className,
                              Object params[],
                              String signature[],
                              ClassLoader loader)
            throws ReflectionException,
            MBeanException{
        Class<?> theClass=findClassWithDefaultLoaderRepository(className);
        return instantiate(theClass,params,signature,loader);
    }

    public Class<?> findClassWithDefaultLoaderRepository(String className)
            throws ReflectionException{
        Class<?> theClass;
        if(className==null){
            throw new RuntimeOperationsException(new
                    IllegalArgumentException("The class name cannot be null"),
                    "Exception occurred during object instantiation");
        }
        ReflectUtil.checkPackageAccess(className);
        try{
            if(clr==null) throw new ClassNotFoundException(className);
            theClass=clr.loadClass(className);
        }catch(ClassNotFoundException ee){
            throw new ReflectionException(ee,
                    "The MBean class could not be loaded by the default loader repository");
        }
        return theClass;
    }

    public Object instantiate(Class<?> theClass,Object params[],
                              String signature[],ClassLoader loader)
            throws ReflectionException, MBeanException{
        checkMBeanPermission(theClass,null,null,"instantiate");
        // Instantiate the new object
        // ------------------------------
        // ------------------------------
        final Class<?>[] tab;
        Object moi;
        try{
            // Build the signature of the method
            //
            ClassLoader aLoader=theClass.getClassLoader();
            // Build the signature of the method
            //
            tab=
                    ((signature==null)?null:
                            findSignatureClasses(signature,aLoader));
        }
        // Exception IllegalArgumentException raised in Jdk1.1.8
        catch(IllegalArgumentException e){
            throw new ReflectionException(e,
                    "The constructor parameter classes could not be loaded");
        }
        // Query the metadata service to get the right constructor
        Constructor<?> cons=findConstructor(theClass,tab);
        if(cons==null){
            throw new ReflectionException(new
                    NoSuchMethodException("No such constructor"));
        }
        try{
            ReflectUtil.checkPackageAccess(theClass);
            ensureClassAccess(theClass);
            moi=cons.newInstance(params);
        }catch(NoSuchMethodError error){
            throw new ReflectionException(new
                    NoSuchMethodException("No such constructor found"),
                    "No such constructor");
        }catch(InstantiationException e){
            throw new ReflectionException(e,
                    "Exception thrown trying to invoke the MBean's constructor");
        }catch(IllegalAccessException e){
            throw new ReflectionException(e,
                    "Exception thrown trying to invoke the MBean's constructor");
        }catch(InvocationTargetException e){
            // Wrap the exception.
            Throwable th=e.getTargetException();
            if(th instanceof RuntimeException){
                throw new RuntimeMBeanException((RuntimeException)th,
                        "RuntimeException thrown in the MBean's constructor");
            }else if(th instanceof Error){
                throw new RuntimeErrorException((Error)th,
                        "Error thrown in the MBean's constructor");
            }else{
                throw new MBeanException((Exception)th,
                        "Exception thrown in the MBean's constructor");
            }
        }
        return moi;
    }

    public Class<?>[] findSignatureClasses(String signature[],
                                           ClassLoader loader)
            throws ReflectionException{
        if(signature==null) return null;
        final ClassLoader aLoader=loader;
        final int length=signature.length;
        final Class<?> tab[]=new Class<?>[length];
        if(length==0) return tab;
        try{
            for(int i=0;i<length;i++){
                // Start handling primitive types (int. boolean and so
                // forth)
                //
                final Class<?> primCla=primitiveClasses.get(signature[i]);
                if(primCla!=null){
                    tab[i]=primCla;
                    continue;
                }
                ReflectUtil.checkPackageAccess(signature[i]);
                // Ok we do not have a primitive type ! We need to build
                // the signature of the method
                //
                if(aLoader!=null){
                    // We need to load the class through the class
                    // loader of the target object.
                    //
                    tab[i]=Class.forName(signature[i],false,aLoader);
                }else{
                    // Load through the default class loader
                    //
                    tab[i]=findClass(signature[i],
                            this.getClass().getClassLoader());
                }
            }
        }catch(ClassNotFoundException e){
            if(MBEANSERVER_LOGGER.isLoggable(Level.FINEST)){
                MBEANSERVER_LOGGER.logp(Level.FINEST,
                        MBeanInstantiator.class.getName(),
                        "findSignatureClasses",
                        "The parameter class could not be found",e);
            }
            throw new ReflectionException(e,
                    "The parameter class could not be found");
        }catch(RuntimeException e){
            if(MBEANSERVER_LOGGER.isLoggable(Level.FINEST)){
                MBEANSERVER_LOGGER.logp(Level.FINEST,
                        MBeanInstantiator.class.getName(),
                        "findSignatureClasses",
                        "Unexpected exception",e);
            }
            throw e;
        }
        return tab;
    }

    public Object instantiate(String className,ObjectName loaderName,
                              ClassLoader loader)
            throws ReflectionException, MBeanException,
            InstanceNotFoundException{
        return instantiate(className,loaderName,(Object[])null,
                (String[])null,loader);
    }

    public Object instantiate(String className,
                              ObjectName loaderName,
                              Object params[],
                              String signature[],
                              ClassLoader loader)
            throws ReflectionException,
            MBeanException,
            InstanceNotFoundException{
        // ------------------------------
        // ------------------------------
        Class<?> theClass;
        if(loaderName==null){
            theClass=findClass(className,loader);
        }else{
            theClass=findClass(className,loaderName);
        }
        return instantiate(theClass,params,signature,loader);
    }

    public ModifiableClassLoaderRepository getClassLoaderRepository(){
        checkMBeanPermission((String)null,null,null,"getClassLoaderRepository");
        return clr;
    }
}
