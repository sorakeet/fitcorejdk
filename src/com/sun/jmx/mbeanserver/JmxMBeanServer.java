/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.mbeanserver;

import com.sun.jmx.interceptor.DefaultMBeanServerInterceptor;

import javax.management.*;
import javax.management.loading.ClassLoaderRepository;
import java.io.ObjectInputStream;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.MBEANSERVER_LOGGER;

public final class JmxMBeanServer
        implements SunJmxMBeanServer{
    public static final boolean DEFAULT_FAIR_LOCK_POLICY=true;
    private final MBeanInstantiator instantiator;
    private final SecureClassLoaderRepository secureClr;
    private final boolean interceptorsEnabled;
    private final MBeanServer outerShell;
    private final MBeanServerDelegate mBeanServerDelegateObject;
    private volatile MBeanServer mbsInterceptor=null;

    JmxMBeanServer(String domain,MBeanServer outer,
                   MBeanServerDelegate delegate){
        this(domain,outer,delegate,null,false);
    }

    JmxMBeanServer(String domain,MBeanServer outer,
                   MBeanServerDelegate delegate,
                   MBeanInstantiator instantiator,
                   boolean interceptors){
        this(domain,outer,delegate,instantiator,interceptors,true);
    }

    JmxMBeanServer(String domain,MBeanServer outer,
                   MBeanServerDelegate delegate,
                   MBeanInstantiator instantiator,
                   boolean interceptors,
                   boolean fairLock){
        if(instantiator==null){
            final ModifiableClassLoaderRepository
                    clr=new ClassLoaderRepositorySupport();
            instantiator=new MBeanInstantiator(clr);
        }
        final MBeanInstantiator fInstantiator=instantiator;
        this.secureClr=new
                SecureClassLoaderRepository(AccessController.doPrivileged(new PrivilegedAction<ClassLoaderRepository>(){
            @Override
            public ClassLoaderRepository run(){
                return fInstantiator.getClassLoaderRepository();
            }
        })
        );
        if(delegate==null)
            delegate=new MBeanServerDelegateImpl();
        if(outer==null)
            outer=this;
        this.instantiator=instantiator;
        this.mBeanServerDelegateObject=delegate;
        this.outerShell=outer;
        final Repository repository=new Repository(domain);
        this.mbsInterceptor=
                new DefaultMBeanServerInterceptor(outer,delegate,instantiator,
                        repository);
        this.interceptorsEnabled=interceptors;
        initialize();
    }

    private void initialize(){
        if(instantiator==null) throw new
                IllegalStateException("instantiator must not be null.");
        // Registers the MBeanServer identification MBean
        try{
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>(){
                public Object run() throws Exception{
                    mbsInterceptor.registerMBean(
                            mBeanServerDelegateObject,
                            MBeanServerDelegate.DELEGATE_NAME);
                    return null;
                }
            });
        }catch(SecurityException e){
            if(MBEANSERVER_LOGGER.isLoggable(Level.FINEST)){
                MBEANSERVER_LOGGER.logp(Level.FINEST,
                        JmxMBeanServer.class.getName(),"initialize",
                        "Unexpected security exception occurred",e);
            }
            throw e;
        }catch(Exception e){
            if(MBEANSERVER_LOGGER.isLoggable(Level.FINEST)){
                MBEANSERVER_LOGGER.logp(Level.FINEST,
                        JmxMBeanServer.class.getName(),"initialize",
                        "Unexpected exception occurred",e);
            }
            throw new
                    IllegalStateException("Can't register delegate.",e);
        }
        /** Add my class loader to the repository
         This can be null if my class loader is the bootstrap
         class loader.  The ClassLoaderRepository knows how
         to handle that case.  */
        ClassLoader myLoader=outerShell.getClass().getClassLoader();
        final ModifiableClassLoaderRepository loaders=AccessController.doPrivileged(new PrivilegedAction<ModifiableClassLoaderRepository>(){
            @Override
            public ModifiableClassLoaderRepository run(){
                return instantiator.getClassLoaderRepository();
            }
        });
        if(loaders!=null){
            loaders.addClassLoader(myLoader);
            /** Add the system class loader, so that if the MBean server is
             loaded by the bootstrap class loader we can still load
             MBeans from the classpath using
             createMBean(className, objectName).

             If this class (JmxMBeanServer) was not loaded by the
             system class loader or a parent of it, then the caller
             must have RuntimePermission("getClassLoader") for the
             getSystemClassLoader() call to succeed.  If the caller
             does not have that permission, any call to
             Class.getClassLoader() will fail.  Since there are lots
             of those in JMX, we better throw the exception now.

             This permission question is irrelevant when JMX is part
             of J2SE (as of 1.5). */
            ClassLoader systemLoader=ClassLoader.getSystemClassLoader();
            if(systemLoader!=myLoader)
                loaders.addClassLoader(systemLoader);
        }
    }

    JmxMBeanServer(String domain,MBeanServer outer,
                   MBeanServerDelegate delegate,boolean interceptors){
        this(domain,outer,delegate,null,false);
    }

    public static MBeanServerDelegate newMBeanServerDelegate(){
        return new MBeanServerDelegateImpl();
    }

    public static MBeanServer newMBeanServer(String defaultDomain,
                                             MBeanServer outer,
                                             MBeanServerDelegate delegate,
                                             boolean interceptors){
        // Determine whether to use fair locking for the repository.
        // Default is true.
        final boolean fairLock=DEFAULT_FAIR_LOCK_POLICY;
        checkNewMBeanServerPermission();
        // This constructor happens to disregard the value of the interceptors
        // flag - that is, it always uses the default value - false.
        // This is admitedly a bug, but we chose not to fix it for now
        // since we would rather not have anybody depending on the Sun private
        // interceptor APIs - which is most probably going to be removed and
        // replaced by a public (javax) feature in the future.
        //
        return new JmxMBeanServer(defaultDomain,outer,delegate,null,
                interceptors,fairLock);
    }

    private static void checkNewMBeanServerPermission(){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            Permission perm=new MBeanServerPermission("newMBeanServer");
            sm.checkPermission(perm);
        }
    }

    public MBeanInstantiator getMBeanInstantiator(){
        if(interceptorsEnabled) return instantiator;
        else throw new UnsupportedOperationException(
                "MBeanServerInterceptors are disabled.");
    }

    public boolean interceptorsEnabled(){
        return interceptorsEnabled;
    }

    public synchronized MBeanServer getMBeanServerInterceptor(){
        if(interceptorsEnabled) return mbsInterceptor;
        else throw new UnsupportedOperationException(
                "MBeanServerInterceptors are disabled.");
    }

    public synchronized void
    setMBeanServerInterceptor(MBeanServer interceptor){
        if(!interceptorsEnabled) throw new UnsupportedOperationException(
                "MBeanServerInterceptors are disabled.");
        if(interceptor==null) throw new
                IllegalArgumentException("MBeanServerInterceptor is null");
        mbsInterceptor=interceptor;
    }

    public MBeanServerDelegate getMBeanServerDelegate(){
        if(!interceptorsEnabled) throw new UnsupportedOperationException(
                "MBeanServerInterceptors are disabled.");
        return mBeanServerDelegateObject;
    }

    public ObjectInstance createMBean(String className,ObjectName name)
            throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException,
            NotCompliantMBeanException{
        return mbsInterceptor.createMBean(className,
                cloneObjectName(name),
                (Object[])null,
                (String[])null);
    }

    public ObjectInstance createMBean(String className,ObjectName name,
                                      ObjectName loaderName)
            throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException,
            NotCompliantMBeanException, InstanceNotFoundException{
        return mbsInterceptor.createMBean(className,
                cloneObjectName(name),
                loaderName,
                (Object[])null,
                (String[])null);
    }

    public ObjectInstance createMBean(String className,ObjectName name,
                                      Object params[],String signature[])
            throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException,
            NotCompliantMBeanException{
        return mbsInterceptor.createMBean(className,cloneObjectName(name),
                params,signature);
    }

    public ObjectInstance createMBean(String className,ObjectName name,
                                      ObjectName loaderName,Object params[],
                                      String signature[])
            throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException,
            NotCompliantMBeanException, InstanceNotFoundException{
        return mbsInterceptor.createMBean(className,cloneObjectName(name),
                loaderName,params,signature);
    }

    public ObjectInstance registerMBean(Object object,ObjectName name)
            throws InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException{
        return mbsInterceptor.registerMBean(object,cloneObjectName(name));
    }

    public void unregisterMBean(ObjectName name)
            throws InstanceNotFoundException, MBeanRegistrationException{
        mbsInterceptor.unregisterMBean(cloneObjectName(name));
    }

    public ObjectInstance getObjectInstance(ObjectName name)
            throws InstanceNotFoundException{
        return mbsInterceptor.getObjectInstance(cloneObjectName(name));
    }

    public Set<ObjectInstance> queryMBeans(ObjectName name,QueryExp query){
        return mbsInterceptor.queryMBeans(cloneObjectName(name),query);
    }

    public Set<ObjectName> queryNames(ObjectName name,QueryExp query){
        return mbsInterceptor.queryNames(cloneObjectName(name),query);
    }

    public boolean isRegistered(ObjectName name){
        return mbsInterceptor.isRegistered(name);
    }

    public Integer getMBeanCount(){
        return mbsInterceptor.getMBeanCount();
    }

    public Object getAttribute(ObjectName name,String attribute)
            throws MBeanException, AttributeNotFoundException,
            InstanceNotFoundException, ReflectionException{
        return mbsInterceptor.getAttribute(cloneObjectName(name),attribute);
    }

    public AttributeList getAttributes(ObjectName name,String[] attributes)
            throws InstanceNotFoundException, ReflectionException{
        return mbsInterceptor.getAttributes(cloneObjectName(name),attributes);
    }

    public void setAttribute(ObjectName name,Attribute attribute)
            throws InstanceNotFoundException, AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException,
            ReflectionException{
        mbsInterceptor.setAttribute(cloneObjectName(name),
                cloneAttribute(attribute));
    }

    public AttributeList setAttributes(ObjectName name,
                                       AttributeList attributes)
            throws InstanceNotFoundException, ReflectionException{
        return mbsInterceptor.setAttributes(cloneObjectName(name),
                cloneAttributeList(attributes));
    }

    public Object invoke(ObjectName name,String operationName,
                         Object params[],String signature[])
            throws InstanceNotFoundException, MBeanException,
            ReflectionException{
        return mbsInterceptor.invoke(cloneObjectName(name),operationName,
                params,signature);
    }

    public String getDefaultDomain(){
        return mbsInterceptor.getDefaultDomain();
    }

    // From MBeanServer
    public String[] getDomains(){
        return mbsInterceptor.getDomains();
    }

    public void addNotificationListener(ObjectName name,
                                        NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback)
            throws InstanceNotFoundException{
        mbsInterceptor.addNotificationListener(cloneObjectName(name),listener,
                filter,handback);
    }

    public void addNotificationListener(ObjectName name,ObjectName listener,
                                        NotificationFilter filter,Object handback)
            throws InstanceNotFoundException{
        mbsInterceptor.addNotificationListener(cloneObjectName(name),listener,
                filter,handback);
    }

    public void removeNotificationListener(ObjectName name,
                                           ObjectName listener)
            throws InstanceNotFoundException, ListenerNotFoundException{
        mbsInterceptor.removeNotificationListener(cloneObjectName(name),
                listener);
    }

    public void removeNotificationListener(ObjectName name,
                                           ObjectName listener,
                                           NotificationFilter filter,
                                           Object handback)
            throws InstanceNotFoundException, ListenerNotFoundException{
        mbsInterceptor.removeNotificationListener(cloneObjectName(name),
                listener,filter,handback);
    }

    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener)
            throws InstanceNotFoundException, ListenerNotFoundException{
        mbsInterceptor.removeNotificationListener(cloneObjectName(name),
                listener);
    }

    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
            throws InstanceNotFoundException, ListenerNotFoundException{
        mbsInterceptor.removeNotificationListener(cloneObjectName(name),
                listener,filter,handback);
    }

    public MBeanInfo getMBeanInfo(ObjectName name) throws
            InstanceNotFoundException, IntrospectionException, ReflectionException{
        return mbsInterceptor.getMBeanInfo(cloneObjectName(name));
    }

    public boolean isInstanceOf(ObjectName name,String className)
            throws InstanceNotFoundException{
        return mbsInterceptor.isInstanceOf(cloneObjectName(name),className);
    }

    public Object instantiate(String className)
            throws ReflectionException, MBeanException{
        /** Permission check */
        checkMBeanPermission(className,null,null,"instantiate");
        return instantiator.instantiate(className);
    }

    public Object instantiate(String className,ObjectName loaderName)
            throws ReflectionException, MBeanException,
            InstanceNotFoundException{
        /** Permission check */
        checkMBeanPermission(className,null,null,"instantiate");
        ClassLoader myLoader=outerShell.getClass().getClassLoader();
        return instantiator.instantiate(className,loaderName,myLoader);
    }

    public Object instantiate(String className,Object params[],
                              String signature[])
            throws ReflectionException, MBeanException{
        /** Permission check */
        checkMBeanPermission(className,null,null,"instantiate");
        ClassLoader myLoader=outerShell.getClass().getClassLoader();
        return instantiator.instantiate(className,params,signature,
                myLoader);
    }

    public Object instantiate(String className,ObjectName loaderName,
                              Object params[],String signature[])
            throws ReflectionException, MBeanException,
            InstanceNotFoundException{
        /** Permission check */
        checkMBeanPermission(className,null,null,"instantiate");
        ClassLoader myLoader=outerShell.getClass().getClassLoader();
        return instantiator.instantiate(className,loaderName,params,signature,
                myLoader);
    }

    @Deprecated
    public ObjectInputStream deserialize(ObjectName name,byte[] data)
            throws InstanceNotFoundException, OperationsException{
        /** Permission check */
        // This call requires MBeanPermission 'getClassLoaderFor'
        final ClassLoader loader=getClassLoaderFor(name);
        return instantiator.deserialize(loader,data);
    }

    @Deprecated
    public ObjectInputStream deserialize(String className,byte[] data)
            throws OperationsException, ReflectionException{
        if(className==null){
            throw new RuntimeOperationsException(
                    new IllegalArgumentException(),
                    "Null className passed in parameter");
        }
        /** Permission check */
        // This call requires MBeanPermission 'getClassLoaderRepository'
        final ClassLoaderRepository clr=getClassLoaderRepository();
        Class<?> theClass;
        try{
            if(clr==null) throw new ClassNotFoundException(className);
            theClass=clr.loadClass(className);
        }catch(ClassNotFoundException e){
            throw new ReflectionException(e,
                    "The given class could not be "+
                            "loaded by the default loader "+
                            "repository");
        }
        return instantiator.deserialize(theClass.getClassLoader(),data);
    }

    @Deprecated
    public ObjectInputStream deserialize(String className,
                                         ObjectName loaderName,
                                         byte[] data) throws
            InstanceNotFoundException, OperationsException, ReflectionException{
        // Clone ObjectName
        //
        loaderName=cloneObjectName(loaderName);
        /** Permission check */
        // Make this call just to force the 'getClassLoader'
        // permission check
        try{
            getClassLoader(loaderName);
        }catch(SecurityException e){
            throw e;
        }catch(Exception e){
        }
        ClassLoader myLoader=outerShell.getClass().getClassLoader();
        return instantiator.deserialize(className,loaderName,data,myLoader);
    }
    // These methods are called by the JMX MBeanServerBuilder.

    public ClassLoader getClassLoaderFor(ObjectName mbeanName)
            throws InstanceNotFoundException{
        return mbsInterceptor.getClassLoaderFor(cloneObjectName(mbeanName));
    }

    public ClassLoader getClassLoader(ObjectName loaderName)
            throws InstanceNotFoundException{
        return mbsInterceptor.getClassLoader(cloneObjectName(loaderName));
    }
    // JMX OBJECT CLONING
    //-------------------

    public ClassLoaderRepository getClassLoaderRepository(){
        /** Permission check */
        checkMBeanPermission(null,null,null,"getClassLoaderRepository");
        return secureClr;
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

    private AttributeList cloneAttributeList(AttributeList list){
        if(list!=null){
            List<Attribute> alist=list.asList();
            if(!list.getClass().equals(AttributeList.class)){
                // Create new attribute list
                //
                AttributeList newList=new AttributeList(alist.size());
                // Iterate through list and replace non JMX attributes
                //
                for(Attribute attribute : alist)
                    newList.add(cloneAttribute(attribute));
                return newList;
            }else{
                // Iterate through list and replace non JMX attributes
                //
                for(int i=0;i<alist.size();i++){
                    Attribute attribute=alist.get(i);
                    if(!attribute.getClass().equals(Attribute.class)){
                        list.set(i,cloneAttribute(attribute));
                    }
                }
                return list;
            }
        }
        return list;
    }
    // SECURITY CHECKS
    //----------------

    private Attribute cloneAttribute(Attribute attribute){
        if(attribute!=null){
            if(!attribute.getClass().equals(Attribute.class)){
                return new Attribute(attribute.getName(),attribute.getValue());
            }
        }
        return attribute;
    }

    private ObjectName cloneObjectName(ObjectName name){
        if(name!=null){
            return ObjectName.getInstance(name);
        }
        return name;
    }
}
