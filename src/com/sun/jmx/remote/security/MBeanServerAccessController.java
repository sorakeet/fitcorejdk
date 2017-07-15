/**
 * Copyright (c) 2003, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.remote.security;

import com.sun.jmx.mbeanserver.GetPropertyAction;

import javax.management.*;
import javax.management.loading.ClassLoaderRepository;
import javax.management.remote.MBeanServerForwarder;
import java.io.ObjectInputStream;
import java.security.AccessController;
import java.util.Set;

public abstract class MBeanServerAccessController
        implements MBeanServerForwarder{
    //------------------
    // PRIVATE VARIABLES
    //------------------
    private MBeanServer mbs;

    public ObjectInstance createMBean(String className,ObjectName name)
            throws
            ReflectionException,
            InstanceAlreadyExistsException,
            MBeanRegistrationException,
            MBeanException,
            NotCompliantMBeanException{
        checkCreate(className);
        SecurityManager sm=System.getSecurityManager();
        if(sm==null){
            Object object=getMBeanServer().instantiate(className);
            checkClassLoader(object);
            return getMBeanServer().registerMBean(object,name);
        }else{
            return getMBeanServer().createMBean(className,name);
        }
    }

    protected void checkCreate(String className){
        checkWrite();
    }

    protected abstract void checkWrite();

    public ObjectInstance createMBean(String className,
                                      ObjectName name,
                                      ObjectName loaderName)
            throws
            ReflectionException,
            InstanceAlreadyExistsException,
            MBeanRegistrationException,
            MBeanException,
            NotCompliantMBeanException,
            InstanceNotFoundException{
        checkCreate(className);
        SecurityManager sm=System.getSecurityManager();
        if(sm==null){
            Object object=getMBeanServer().instantiate(className,
                    loaderName);
            checkClassLoader(object);
            return getMBeanServer().registerMBean(object,name);
        }else{
            return getMBeanServer().createMBean(className,name,loaderName);
        }
    }

    public ObjectInstance createMBean(String className,ObjectName name,
                                      Object params[],String signature[])
            throws
            ReflectionException,
            InstanceAlreadyExistsException,
            MBeanRegistrationException,
            MBeanException,
            NotCompliantMBeanException{
        checkCreate(className);
        SecurityManager sm=System.getSecurityManager();
        if(sm==null){
            Object object=getMBeanServer().instantiate(className,
                    params,
                    signature);
            checkClassLoader(object);
            return getMBeanServer().registerMBean(object,name);
        }else{
            return getMBeanServer().createMBean(className,name,
                    params,signature);
        }
    }
    //--------------------------------------------
    //--------------------------------------------
    //
    // Implementation of the MBeanServer interface
    //
    //--------------------------------------------
    //--------------------------------------------

    public ObjectInstance createMBean(String className,
                                      ObjectName name,
                                      ObjectName loaderName,
                                      Object params[],
                                      String signature[])
            throws
            ReflectionException,
            InstanceAlreadyExistsException,
            MBeanRegistrationException,
            MBeanException,
            NotCompliantMBeanException,
            InstanceNotFoundException{
        checkCreate(className);
        SecurityManager sm=System.getSecurityManager();
        if(sm==null){
            Object object=getMBeanServer().instantiate(className,
                    loaderName,
                    params,
                    signature);
            checkClassLoader(object);
            return getMBeanServer().registerMBean(object,name);
        }else{
            return getMBeanServer().createMBean(className,name,loaderName,
                    params,signature);
        }
    }

    public ObjectInstance registerMBean(Object object,ObjectName name)
            throws
            InstanceAlreadyExistsException,
            MBeanRegistrationException,
            NotCompliantMBeanException{
        checkWrite();
        return getMBeanServer().registerMBean(object,name);
    }

    public void unregisterMBean(ObjectName name)
            throws InstanceNotFoundException, MBeanRegistrationException{
        checkUnregister(name);
        getMBeanServer().unregisterMBean(name);
    }

    protected void checkUnregister(ObjectName name){
        checkWrite();
    }

    public ObjectInstance getObjectInstance(ObjectName name)
            throws InstanceNotFoundException{
        checkRead();
        return getMBeanServer().getObjectInstance(name);
    }

    public Set<ObjectInstance> queryMBeans(ObjectName name,QueryExp query){
        checkRead();
        return getMBeanServer().queryMBeans(name,query);
    }

    public Set<ObjectName> queryNames(ObjectName name,QueryExp query){
        checkRead();
        return getMBeanServer().queryNames(name,query);
    }

    public boolean isRegistered(ObjectName name){
        checkRead();
        return getMBeanServer().isRegistered(name);
    }

    public Integer getMBeanCount(){
        checkRead();
        return getMBeanServer().getMBeanCount();
    }

    public Object getAttribute(ObjectName name,String attribute)
            throws
            MBeanException,
            AttributeNotFoundException,
            InstanceNotFoundException,
            ReflectionException{
        checkRead();
        return getMBeanServer().getAttribute(name,attribute);
    }

    public AttributeList getAttributes(ObjectName name,String[] attributes)
            throws InstanceNotFoundException, ReflectionException{
        checkRead();
        return getMBeanServer().getAttributes(name,attributes);
    }

    public void setAttribute(ObjectName name,Attribute attribute)
            throws
            InstanceNotFoundException,
            AttributeNotFoundException,
            InvalidAttributeValueException,
            MBeanException,
            ReflectionException{
        checkWrite();
        getMBeanServer().setAttribute(name,attribute);
    }

    public AttributeList setAttributes(ObjectName name,
                                       AttributeList attributes)
            throws InstanceNotFoundException, ReflectionException{
        checkWrite();
        return getMBeanServer().setAttributes(name,attributes);
    }

    public Object invoke(ObjectName name,String operationName,
                         Object params[],String signature[])
            throws
            InstanceNotFoundException,
            MBeanException,
            ReflectionException{
        checkWrite();
        checkMLetMethods(name,operationName);
        return getMBeanServer().invoke(name,operationName,params,signature);
    }

    public String getDefaultDomain(){
        checkRead();
        return getMBeanServer().getDefaultDomain();
    }

    public String[] getDomains(){
        checkRead();
        return getMBeanServer().getDomains();
    }

    public void addNotificationListener(ObjectName name,
                                        NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback)
            throws InstanceNotFoundException{
        checkRead();
        getMBeanServer().addNotificationListener(name,listener,
                filter,handback);
    }

    public MBeanServer getMBeanServer(){
        return mbs;
    }

    public void setMBeanServer(MBeanServer mbs){
        if(mbs==null)
            throw new IllegalArgumentException("Null MBeanServer");
        if(this.mbs!=null)
            throw new IllegalArgumentException("MBeanServer object already "+
                    "initialized");
        this.mbs=mbs;
    }

    protected abstract void checkRead();

    public void addNotificationListener(ObjectName name,
                                        ObjectName listener,
                                        NotificationFilter filter,
                                        Object handback)
            throws InstanceNotFoundException{
        checkRead();
        getMBeanServer().addNotificationListener(name,listener,
                filter,handback);
    }

    public void removeNotificationListener(ObjectName name,
                                           ObjectName listener)
            throws InstanceNotFoundException, ListenerNotFoundException{
        checkRead();
        getMBeanServer().removeNotificationListener(name,listener);
    }

    public void removeNotificationListener(ObjectName name,
                                           ObjectName listener,
                                           NotificationFilter filter,
                                           Object handback)
            throws InstanceNotFoundException, ListenerNotFoundException{
        checkRead();
        getMBeanServer().removeNotificationListener(name,listener,
                filter,handback);
    }

    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener)
            throws InstanceNotFoundException, ListenerNotFoundException{
        checkRead();
        getMBeanServer().removeNotificationListener(name,listener);
    }

    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
            throws InstanceNotFoundException, ListenerNotFoundException{
        checkRead();
        getMBeanServer().removeNotificationListener(name,listener,
                filter,handback);
    }

    public MBeanInfo getMBeanInfo(ObjectName name)
            throws
            InstanceNotFoundException,
            IntrospectionException,
            ReflectionException{
        checkRead();
        return getMBeanServer().getMBeanInfo(name);
    }

    public boolean isInstanceOf(ObjectName name,String className)
            throws InstanceNotFoundException{
        checkRead();
        return getMBeanServer().isInstanceOf(name,className);
    }

    public Object instantiate(String className)
            throws ReflectionException, MBeanException{
        checkCreate(className);
        return getMBeanServer().instantiate(className);
    }

    public Object instantiate(String className,ObjectName loaderName)
            throws ReflectionException, MBeanException, InstanceNotFoundException{
        checkCreate(className);
        return getMBeanServer().instantiate(className,loaderName);
    }

    public Object instantiate(String className,
                              Object params[],
                              String signature[])
            throws ReflectionException, MBeanException{
        checkCreate(className);
        return getMBeanServer().instantiate(className,params,signature);
    }

    public Object instantiate(String className,ObjectName loaderName,
                              Object params[],String signature[])
            throws ReflectionException, MBeanException, InstanceNotFoundException{
        checkCreate(className);
        return getMBeanServer().instantiate(className,loaderName,
                params,signature);
    }

    @Deprecated
    public ObjectInputStream deserialize(ObjectName name,byte[] data)
            throws InstanceNotFoundException, OperationsException{
        checkRead();
        return getMBeanServer().deserialize(name,data);
    }

    @Deprecated
    public ObjectInputStream deserialize(String className,byte[] data)
            throws OperationsException, ReflectionException{
        checkRead();
        return getMBeanServer().deserialize(className,data);
    }

    @Deprecated
    public ObjectInputStream deserialize(String className,
                                         ObjectName loaderName,
                                         byte[] data)
            throws
            InstanceNotFoundException,
            OperationsException,
            ReflectionException{
        checkRead();
        return getMBeanServer().deserialize(className,loaderName,data);
    }

    public ClassLoader getClassLoaderFor(ObjectName mbeanName)
            throws InstanceNotFoundException{
        checkRead();
        return getMBeanServer().getClassLoaderFor(mbeanName);
    }

    public ClassLoader getClassLoader(ObjectName loaderName)
            throws InstanceNotFoundException{
        checkRead();
        return getMBeanServer().getClassLoader(loaderName);
    }
    //----------------
    // PRIVATE METHODS
    //----------------

    public ClassLoaderRepository getClassLoaderRepository(){
        checkRead();
        return getMBeanServer().getClassLoaderRepository();
    }

    private void checkMLetMethods(ObjectName name,String operation)
            throws InstanceNotFoundException{
        // Check if security manager installed
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            return;
        }
        // Check for addURL and getMBeansFromURL methods
        if(!operation.equals("addURL")&&
                !operation.equals("getMBeansFromURL")){
            return;
        }
        // Check if MBean is instance of MLet
        if(!getMBeanServer().isInstanceOf(name,
                "javax.management.loading.MLet")){
            return;
        }
        // Throw security exception
        if(operation.equals("addURL")){ // addURL
            throw new SecurityException("Access denied! MLet method addURL "+
                    "cannot be invoked unless a security manager is installed.");
        }else{ // getMBeansFromURL
            // Whether or not calling getMBeansFromURL is allowed is controlled
            // by the value of the "jmx.remote.x.mlet.allow.getMBeansFromURL"
            // system property. If the value of this property is true, calling
            // the MLet's getMBeansFromURL method is allowed. The default value
            // for this property is false.
            final String propName="jmx.remote.x.mlet.allow.getMBeansFromURL";
            GetPropertyAction propAction=new GetPropertyAction(propName);
            String propValue=AccessController.doPrivileged(propAction);
            boolean allowGetMBeansFromURL="true".equalsIgnoreCase(propValue);
            if(!allowGetMBeansFromURL){
                throw new SecurityException("Access denied! MLet method "+
                        "getMBeansFromURL cannot be invoked unless a "+
                        "security manager is installed or the system property "+
                        "-Djmx.remote.x.mlet.allow.getMBeansFromURL=true "+
                        "is specified.");
            }
        }
    }

    private void checkClassLoader(Object object){
        if(object instanceof ClassLoader)
            throw new SecurityException("Access denied! Creating an "+
                    "MBean that is a ClassLoader "+
                    "is forbidden unless a security "+
                    "manager is installed.");
    }
}
