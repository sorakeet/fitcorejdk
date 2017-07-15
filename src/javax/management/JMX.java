/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import com.sun.jmx.mbeanserver.Introspector;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

public class JMX{
    public static final String DEFAULT_VALUE_FIELD="defaultValue";
    public static final String IMMUTABLE_INFO_FIELD="immutableInfo";
    public static final String INTERFACE_CLASS_NAME_FIELD="interfaceClassName";
    public static final String LEGAL_VALUES_FIELD="legalValues";
    public static final String MAX_VALUE_FIELD="maxValue";
    public static final String MIN_VALUE_FIELD="minValue";
    public static final String MXBEAN_FIELD="mxbean";
    public static final String OPEN_TYPE_FIELD="openType";
    public static final String ORIGINAL_TYPE_FIELD="originalType";
    static final JMX proof=new JMX();
    private JMX(){
    }

    public static <T> T newMBeanProxy(MBeanServerConnection connection,
                                      ObjectName objectName,
                                      Class<T> interfaceClass){
        return newMBeanProxy(connection,objectName,interfaceClass,false);
    }

    public static <T> T newMBeanProxy(MBeanServerConnection connection,
                                      ObjectName objectName,
                                      Class<T> interfaceClass,
                                      boolean notificationEmitter){
        return createProxy(connection,objectName,interfaceClass,notificationEmitter,false);
    }

    private static <T> T createProxy(MBeanServerConnection connection,
                                     ObjectName objectName,
                                     Class<T> interfaceClass,
                                     boolean notificationEmitter,
                                     boolean isMXBean){
        try{
            if(isMXBean){
                // Check interface for MXBean compliance
                Introspector.testComplianceMXBeanInterface(interfaceClass);
            }else{
                // Check interface for MBean compliance
                Introspector.testComplianceMBeanInterface(interfaceClass);
            }
        }catch(NotCompliantMBeanException e){
            throw new IllegalArgumentException(e);
        }
        InvocationHandler handler=new MBeanServerInvocationHandler(
                connection,objectName,isMXBean);
        final Class<?>[] interfaces;
        if(notificationEmitter){
            interfaces=
                    new Class<?>[]{interfaceClass,NotificationEmitter.class};
        }else
            interfaces=new Class<?>[]{interfaceClass};
        Object proxy=Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                interfaces,
                handler);
        return interfaceClass.cast(proxy);
    }

    public static <T> T newMXBeanProxy(MBeanServerConnection connection,
                                       ObjectName objectName,
                                       Class<T> interfaceClass){
        return newMXBeanProxy(connection,objectName,interfaceClass,false);
    }

    public static <T> T newMXBeanProxy(MBeanServerConnection connection,
                                       ObjectName objectName,
                                       Class<T> interfaceClass,
                                       boolean notificationEmitter){
        return createProxy(connection,objectName,interfaceClass,notificationEmitter,true);
    }

    public static boolean isMXBeanInterface(Class<?> interfaceClass){
        if(!interfaceClass.isInterface())
            return false;
        if(!Modifier.isPublic(interfaceClass.getModifiers())&&
                !Introspector.ALLOW_NONPUBLIC_MBEAN){
            return false;
        }
        MXBean a=interfaceClass.getAnnotation(MXBean.class);
        if(a!=null)
            return a.value();
        return interfaceClass.getName().endsWith("MXBean");
        // We don't bother excluding the case where the name is
        // exactly the string "MXBean" since that would mean there
        // was no package name, which is pretty unlikely in practice.
    }
}
