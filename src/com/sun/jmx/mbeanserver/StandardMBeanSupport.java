/**
 * Copyright (c) 2005, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.mbeanserver;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import java.lang.reflect.Method;

public class StandardMBeanSupport extends MBeanSupport<Method>{
    public <T> StandardMBeanSupport(T resource,Class<T> mbeanInterfaceType)
            throws NotCompliantMBeanException{
        super(resource,mbeanInterfaceType);
    }

    @Override
    MBeanIntrospector<Method> getMBeanIntrospector(){
        return StandardMBeanIntrospector.getInstance();
    }

    @Override
    Object getCookie(){
        return null;
    }

    @Override
    public void register(MBeanServer mbs,ObjectName name){
    }

    @Override
    public void unregister(){
    }

    @Override
    public MBeanInfo getMBeanInfo(){
        MBeanInfo mbi=super.getMBeanInfo();
        Class<?> resourceClass=getResource().getClass();
        if(StandardMBeanIntrospector.isDefinitelyImmutableInfo(resourceClass))
            return mbi;
        return new MBeanInfo(mbi.getClassName(),mbi.getDescription(),
                mbi.getAttributes(),mbi.getConstructors(),
                mbi.getOperations(),
                MBeanIntrospector.findNotifications(getResource()),
                mbi.getDescriptor());
    }
}
