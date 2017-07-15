/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.mbeanserver;

import sun.reflect.misc.ReflectUtil;

import javax.management.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

import static com.sun.jmx.mbeanserver.Util.newList;

abstract class MBeanIntrospector<M>{
    static boolean isValidParameter(Method m,Object value,int paramNo){
        Class<?> c=m.getParameterTypes()[paramNo];
        try{
            // Following is expensive but we only call this method to determine
            // if an exception is due to an incompatible parameter type.
            // Plain old c.isInstance doesn't work for primitive types.
            Object a=Array.newInstance(c,1);
            Array.set(a,0,value);
            return true;
        }catch(IllegalArgumentException e){
            return false;
        }
    }

    abstract boolean isMXBean();

    abstract M mFrom(Method m);

    abstract String getName(M m);

    abstract String[] getSignature(M m);

    abstract void checkMethod(M m);

    abstract MBeanAttributeInfo getMBeanAttributeInfo(String attributeName,
                                                      M getter,M setter);

    abstract MBeanOperationInfo getMBeanOperationInfo(String operationName,
                                                      M operation);

    abstract Descriptor getBasicMBeanDescriptor();

    final List<Method> getMethods(final Class<?> mbeanType){
        ReflectUtil.checkPackageAccess(mbeanType);
        return Arrays.asList(mbeanType.getMethods());
    }

    final PerInterface<M> getPerInterface(Class<?> mbeanInterface)
            throws NotCompliantMBeanException{
        PerInterfaceMap<M> map=getPerInterfaceMap();
        synchronized(map){
            WeakReference<PerInterface<M>> wr=map.get(mbeanInterface);
            PerInterface<M> pi=(wr==null)?null:wr.get();
            if(pi==null){
                try{
                    MBeanAnalyzer<M> analyzer=getAnalyzer(mbeanInterface);
                    MBeanInfo mbeanInfo=
                            makeInterfaceMBeanInfo(mbeanInterface,analyzer);
                    pi=new PerInterface<M>(mbeanInterface,this,analyzer,
                            mbeanInfo);
                    wr=new WeakReference<PerInterface<M>>(pi);
                    map.put(mbeanInterface,wr);
                }catch(Exception x){
                    throw Introspector.throwException(mbeanInterface,x);
                }
            }
            return pi;
        }
    }

    abstract PerInterfaceMap<M> getPerInterfaceMap();

    abstract MBeanAnalyzer<M> getAnalyzer(Class<?> mbeanInterface)
            throws NotCompliantMBeanException;

    private MBeanInfo makeInterfaceMBeanInfo(Class<?> mbeanInterface,
                                             MBeanAnalyzer<M> analyzer){
        final MBeanInfoMaker maker=new MBeanInfoMaker();
        analyzer.visit(maker);
        final String description=
                "Information on the management interface of the MBean";
        return maker.makeMBeanInfo(mbeanInterface,description);
    }

    final boolean consistent(M getter,M setter){
        return (getter==null||setter==null||
                getGenericReturnType(getter).equals(getGenericParameterTypes(setter)[0]));
    }

    abstract Type getGenericReturnType(M m);

    abstract Type[] getGenericParameterTypes(M m);

    final Object invokeM(M m,Object target,Object[] args,Object cookie)
            throws MBeanException, ReflectionException{
        try{
            return invokeM2(m,target,args,cookie);
        }catch(InvocationTargetException e){
            unwrapInvocationTargetException(e);
            throw new RuntimeException(e); // not reached
        }catch(IllegalAccessException e){
            throw new ReflectionException(e,e.toString());
        }
        /** We do not catch and wrap RuntimeException or Error,
         * because we're in a DynamicMBean, so the logic for DynamicMBeans
         * will do the wrapping.
         */
    }

    abstract Object invokeM2(M m,Object target,Object[] args,Object cookie)
            throws InvocationTargetException, IllegalAccessException,
            MBeanException;

    private static void
    unwrapInvocationTargetException(InvocationTargetException e)
            throws MBeanException{
        Throwable t=e.getCause();
        if(t instanceof RuntimeException)
            throw (RuntimeException)t;
        else if(t instanceof Error)
            throw (Error)t;
        else
            throw new MBeanException((Exception)t,
                    (t==null?null:t.toString()));
    }

    final void invokeSetter(String name,M setter,Object target,Object arg,
                            Object cookie)
            throws MBeanException, ReflectionException,
            InvalidAttributeValueException{
        try{
            invokeM2(setter,target,new Object[]{arg},cookie);
        }catch(IllegalAccessException e){
            throw new ReflectionException(e,e.toString());
        }catch(RuntimeException e){
            maybeInvalidParameter(name,setter,arg,cookie);
            throw e;
        }catch(InvocationTargetException e){
            maybeInvalidParameter(name,setter,arg,cookie);
            unwrapInvocationTargetException(e);
        }
    }

    private void maybeInvalidParameter(String name,M setter,Object arg,
                                       Object cookie)
            throws InvalidAttributeValueException{
        if(!validParameter(setter,arg,0,cookie)){
            final String msg=
                    "Invalid value for attribute "+name+": "+arg;
            throw new InvalidAttributeValueException(msg);
        }
    }

    abstract boolean validParameter(M m,Object value,int paramNo,
                                    Object cookie);

    final MBeanInfo getMBeanInfo(Object resource,PerInterface<M> perInterface){
        MBeanInfo mbi=
                getClassMBeanInfo(resource.getClass(),perInterface);
        MBeanNotificationInfo[] notifs=findNotifications(resource);
        if(notifs==null||notifs.length==0)
            return mbi;
        else{
            return new MBeanInfo(mbi.getClassName(),
                    mbi.getDescription(),
                    mbi.getAttributes(),
                    mbi.getConstructors(),
                    mbi.getOperations(),
                    notifs,
                    mbi.getDescriptor());
        }
    }

    final MBeanInfo getClassMBeanInfo(Class<?> resourceClass,
                                      PerInterface<M> perInterface){
        MBeanInfoMap map=getMBeanInfoMap();
        synchronized(map){
            WeakHashMap<Class<?>,MBeanInfo> intfMap=map.get(resourceClass);
            if(intfMap==null){
                intfMap=new WeakHashMap<Class<?>,MBeanInfo>();
                map.put(resourceClass,intfMap);
            }
            Class<?> intfClass=perInterface.getMBeanInterface();
            MBeanInfo mbi=intfMap.get(intfClass);
            if(mbi==null){
                MBeanInfo imbi=perInterface.getMBeanInfo();
                Descriptor descriptor=
                        ImmutableDescriptor.union(imbi.getDescriptor(),
                                getMBeanDescriptor(resourceClass));
                mbi=new MBeanInfo(resourceClass.getName(),
                        imbi.getDescription(),
                        imbi.getAttributes(),
                        findConstructors(resourceClass),
                        imbi.getOperations(),
                        (MBeanNotificationInfo[])null,
                        descriptor);
                intfMap.put(intfClass,mbi);
            }
            return mbi;
        }
    }

    abstract MBeanInfoMap getMBeanInfoMap();

    abstract Descriptor getMBeanDescriptor(Class<?> resourceClass);

    private static MBeanConstructorInfo[] findConstructors(Class<?> c){
        Constructor<?>[] cons=c.getConstructors();
        MBeanConstructorInfo[] mbc=new MBeanConstructorInfo[cons.length];
        for(int i=0;i<cons.length;i++){
            final String descr="Public constructor of the MBean";
            mbc[i]=new MBeanConstructorInfo(descr,cons[i]);
        }
        return mbc;
    }

    static MBeanNotificationInfo[] findNotifications(Object moi){
        if(!(moi instanceof NotificationBroadcaster))
            return null;
        MBeanNotificationInfo[] mbn=
                ((NotificationBroadcaster)moi).getNotificationInfo();
        if(mbn==null)
            return null;
        MBeanNotificationInfo[] result=
                new MBeanNotificationInfo[mbn.length];
        for(int i=0;i<mbn.length;i++){
            MBeanNotificationInfo ni=mbn[i];
            if(ni.getClass()!=MBeanNotificationInfo.class)
                ni=(MBeanNotificationInfo)ni.clone();
            result[i]=ni;
        }
        return result;
    }

    static final class PerInterfaceMap<M>
            extends WeakHashMap<Class<?>,WeakReference<PerInterface<M>>>{
    }

    static class MBeanInfoMap
            extends WeakHashMap<Class<?>,WeakHashMap<Class<?>,MBeanInfo>>{
    }

    private class MBeanInfoMaker
            implements MBeanAnalyzer.MBeanVisitor<M>{
        private final List<MBeanAttributeInfo> attrs=newList();
        private final List<MBeanOperationInfo> ops=newList();

        public void visitAttribute(String attributeName,
                                   M getter,
                                   M setter){
            MBeanAttributeInfo mbai=
                    getMBeanAttributeInfo(attributeName,getter,setter);
            attrs.add(mbai);
        }

        public void visitOperation(String operationName,
                                   M operation){
            MBeanOperationInfo mboi=
                    getMBeanOperationInfo(operationName,operation);
            ops.add(mboi);
        }

        MBeanInfo makeMBeanInfo(Class<?> mbeanInterface,
                                String description){
            final MBeanAttributeInfo[] attrArray=
                    attrs.toArray(new MBeanAttributeInfo[0]);
            final MBeanOperationInfo[] opArray=
                    ops.toArray(new MBeanOperationInfo[0]);
            final String interfaceClassName=
                    "interfaceClassName="+mbeanInterface.getName();
            final Descriptor classNameDescriptor=
                    new ImmutableDescriptor(interfaceClassName);
            final Descriptor mbeanDescriptor=getBasicMBeanDescriptor();
            final Descriptor annotatedDescriptor=
                    Introspector.descriptorForElement(mbeanInterface);
            final Descriptor descriptor=
                    DescriptorCache.getInstance().union(
                            classNameDescriptor,
                            mbeanDescriptor,
                            annotatedDescriptor);
            return new MBeanInfo(mbeanInterface.getName(),
                    description,
                    attrArray,
                    null,
                    opArray,
                    null,
                    descriptor);
        }
    }
}
