/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.mbeanserver;

import sun.reflect.misc.ReflectUtil;

import javax.management.*;

public abstract class MBeanSupport<M>
        implements DynamicMBean2, MBeanRegistration{
    private final MBeanInfo mbeanInfo;
    private final Object resource;
    private final PerInterface<M> perInterface;    abstract Object getCookie();

    <T> MBeanSupport(T resource,Class<T> mbeanInterfaceType)
            throws NotCompliantMBeanException{
        if(mbeanInterfaceType==null)
            throw new NotCompliantMBeanException("Null MBean interface");
        if(!mbeanInterfaceType.isInstance(resource)){
            final String msg=
                    "Resource class "+resource.getClass().getName()+
                            " is not an instance of "+mbeanInterfaceType.getName();
            throw new NotCompliantMBeanException(msg);
        }
        ReflectUtil.checkPackageAccess(mbeanInterfaceType);
        this.resource=resource;
        MBeanIntrospector<M> introspector=getMBeanIntrospector();
        this.perInterface=introspector.getPerInterface(mbeanInterfaceType);
        this.mbeanInfo=introspector.getMBeanInfo(resource,perInterface);
    }

    abstract MBeanIntrospector<M> getMBeanIntrospector();

    public final boolean isMXBean(){
        return perInterface.isMXBean();
    }

    public final ObjectName preRegister(MBeanServer server,ObjectName name)
            throws Exception{
        if(resource instanceof MBeanRegistration)
            name=((MBeanRegistration)resource).preRegister(server,name);
        return name;
    }

    public final void postRegister(Boolean registrationDone){
        if(resource instanceof MBeanRegistration)
            ((MBeanRegistration)resource).postRegister(registrationDone);
    }

    public final void preDeregister() throws Exception{
        if(resource instanceof MBeanRegistration)
            ((MBeanRegistration)resource).preDeregister();
    }

    public final void postDeregister(){
        // Undo any work from registration.  We do this in postDeregister
        // not preDeregister, because if the user preDeregister throws an
        // exception then the MBean is not unregistered.
        try{
            unregister();
        }finally{
            if(resource instanceof MBeanRegistration)
                ((MBeanRegistration)resource).postDeregister();
        }
    }

    public final Object getResource(){
        return resource;
    }

    public final String getClassName(){
        return resource.getClass().getName();
    }

    public final void preRegister2(MBeanServer server,ObjectName name)
            throws Exception{
        register(server,name);
    }    public final Object getAttribute(String attribute)
            throws AttributeNotFoundException,
            MBeanException,
            ReflectionException{
        return perInterface.getAttribute(resource,attribute,getCookie());
    }

    // Methods that javax.management.StandardMBean should call from its
    // preRegister and postRegister, given that it is not supposed to
    // call the contained object's preRegister etc methods even if it has them
    public abstract void register(MBeanServer mbs,ObjectName name)
            throws Exception;    public final AttributeList getAttributes(String[] attributes){
        final AttributeList result=new AttributeList(attributes.length);
        for(String attrName : attributes){
            try{
                final Object attrValue=getAttribute(attrName);
                result.add(new Attribute(attrName,attrValue));
            }catch(Exception e){
                // OK: attribute is not included in returned list, per spec
                // XXX: log the exception
            }
        }
        return result;
    }

    public final void registerFailed(){
        unregister();
    }    public final void setAttribute(Attribute attribute)
            throws AttributeNotFoundException,
            InvalidAttributeValueException,
            MBeanException,
            ReflectionException{
        final String name=attribute.getName();
        final Object value=attribute.getValue();
        perInterface.setAttribute(resource,name,value,getCookie());
    }

    public abstract void unregister();    public final AttributeList setAttributes(AttributeList attributes){
        final AttributeList result=new AttributeList(attributes.size());
        for(Object attrObj : attributes){
            // We can't use AttributeList.asList because it has side-effects
            Attribute attr=(Attribute)attrObj;
            try{
                setAttribute(attr);
                result.add(new Attribute(attr.getName(),attr.getValue()));
            }catch(Exception e){
                // OK: attribute is not included in returned list, per spec
                // XXX: log the exception
            }
        }
        return result;
    }

    public final Class<?> getMBeanInterface(){
        return perInterface.getMBeanInterface();
    }    public final Object invoke(String operation,Object[] params,
                               String[] signature)
            throws MBeanException, ReflectionException{
        return perInterface.invoke(resource,operation,params,signature,
                getCookie());
    }

    // Overridden by StandardMBeanSupport
    public MBeanInfo getMBeanInfo(){
        return mbeanInfo;
    }










}
