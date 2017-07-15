/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;
// java import

import javax.management.loading.ClassLoaderRepository;
import java.io.ObjectInputStream;
import java.util.Set;
// RI import

public interface MBeanServer extends MBeanServerConnection{
    public ObjectInstance createMBean(String className,ObjectName name)
            throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException,
            NotCompliantMBeanException;

    public ObjectInstance createMBean(String className,ObjectName name,
                                      ObjectName loaderName)
            throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException,
            NotCompliantMBeanException, InstanceNotFoundException;

    public ObjectInstance createMBean(String className,ObjectName name,
                                      Object params[],String signature[])
            throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException,
            NotCompliantMBeanException;

    public ObjectInstance createMBean(String className,ObjectName name,
                                      ObjectName loaderName,Object params[],
                                      String signature[])
            throws ReflectionException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MBeanException,
            NotCompliantMBeanException, InstanceNotFoundException;

    public void unregisterMBean(ObjectName name)
            throws InstanceNotFoundException, MBeanRegistrationException;

    // doc comment inherited from MBeanServerConnection
    public ObjectInstance getObjectInstance(ObjectName name)
            throws InstanceNotFoundException;

    public Set<ObjectInstance> queryMBeans(ObjectName name,QueryExp query);

    public Set<ObjectName> queryNames(ObjectName name,QueryExp query);

    // doc comment inherited from MBeanServerConnection
    public boolean isRegistered(ObjectName name);

    public Integer getMBeanCount();

    // doc comment inherited from MBeanServerConnection
    public Object getAttribute(ObjectName name,String attribute)
            throws MBeanException, AttributeNotFoundException,
            InstanceNotFoundException, ReflectionException;

    // doc comment inherited from MBeanServerConnection
    public AttributeList getAttributes(ObjectName name,String[] attributes)
            throws InstanceNotFoundException, ReflectionException;

    // doc comment inherited from MBeanServerConnection
    public void setAttribute(ObjectName name,Attribute attribute)
            throws InstanceNotFoundException, AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException,
            ReflectionException;

    // doc comment inherited from MBeanServerConnection
    public AttributeList setAttributes(ObjectName name,
                                       AttributeList attributes)
            throws InstanceNotFoundException, ReflectionException;

    // doc comment inherited from MBeanServerConnection
    public Object invoke(ObjectName name,String operationName,
                         Object params[],String signature[])
            throws InstanceNotFoundException, MBeanException,
            ReflectionException;

    // doc comment inherited from MBeanServerConnection
    public String getDefaultDomain();

    // doc comment inherited from MBeanServerConnection
    public String[] getDomains();

    // doc comment inherited from MBeanServerConnection, plus:
    public void addNotificationListener(ObjectName name,
                                        NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback)
            throws InstanceNotFoundException;

    public void addNotificationListener(ObjectName name,
                                        ObjectName listener,
                                        NotificationFilter filter,
                                        Object handback)
            throws InstanceNotFoundException;

    // doc comment inherited from MBeanServerConnection
    public void removeNotificationListener(ObjectName name,
                                           ObjectName listener)
            throws InstanceNotFoundException, ListenerNotFoundException;

    // doc comment inherited from MBeanServerConnection
    public void removeNotificationListener(ObjectName name,
                                           ObjectName listener,
                                           NotificationFilter filter,
                                           Object handback)
            throws InstanceNotFoundException, ListenerNotFoundException;

    // doc comment inherited from MBeanServerConnection
    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener)
            throws InstanceNotFoundException, ListenerNotFoundException;

    // doc comment inherited from MBeanServerConnection
    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
            throws InstanceNotFoundException, ListenerNotFoundException;

    // doc comment inherited from MBeanServerConnection
    public MBeanInfo getMBeanInfo(ObjectName name)
            throws InstanceNotFoundException, IntrospectionException,
            ReflectionException;

    // doc comment inherited from MBeanServerConnection
    public boolean isInstanceOf(ObjectName name,String className)
            throws InstanceNotFoundException;

    public ObjectInstance registerMBean(Object object,ObjectName name)
            throws InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException;

    public Object instantiate(String className)
            throws ReflectionException, MBeanException;

    public Object instantiate(String className,ObjectName loaderName)
            throws ReflectionException, MBeanException,
            InstanceNotFoundException;

    public Object instantiate(String className,Object params[],
                              String signature[])
            throws ReflectionException, MBeanException;

    public Object instantiate(String className,ObjectName loaderName,
                              Object params[],String signature[])
            throws ReflectionException, MBeanException,
            InstanceNotFoundException;

    @Deprecated
    public ObjectInputStream deserialize(ObjectName name,byte[] data)
            throws InstanceNotFoundException, OperationsException;

    @Deprecated
    public ObjectInputStream deserialize(String className,byte[] data)
            throws OperationsException, ReflectionException;

    @Deprecated
    public ObjectInputStream deserialize(String className,
                                         ObjectName loaderName,
                                         byte[] data)
            throws InstanceNotFoundException, OperationsException,
            ReflectionException;

    public ClassLoader getClassLoaderFor(ObjectName mbeanName)
            throws InstanceNotFoundException;

    public ClassLoader getClassLoader(ObjectName loaderName)
            throws InstanceNotFoundException;

    public ClassLoaderRepository getClassLoaderRepository();
}
