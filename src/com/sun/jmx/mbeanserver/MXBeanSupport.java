/**
 * Copyright (c) 2005, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.mbeanserver;

import javax.management.*;
import java.util.Iterator;
import java.util.Set;

import static com.sun.jmx.mbeanserver.Util.newSet;

public class MXBeanSupport extends MBeanSupport<ConvertingMethod>{
    private final Object lock=new Object(); // for mxbeanLookup and objectName
    private MXBeanLookup mxbeanLookup;
    private ObjectName objectName;

    public <T> MXBeanSupport(T resource,Class<T> mxbeanInterface)
            throws NotCompliantMBeanException{
        super(resource,mxbeanInterface);
    }

    static <T> Class<? super T> findMXBeanInterface(Class<T> resourceClass){
        if(resourceClass==null)
            throw new IllegalArgumentException("Null resource class");
        final Set<Class<?>> intfs=transitiveInterfaces(resourceClass);
        final Set<Class<?>> candidates=newSet();
        for(Class<?> intf : intfs){
            if(JMX.isMXBeanInterface(intf))
                candidates.add(intf);
        }
        reduce:
        while(candidates.size()>1){
            for(Class<?> intf : candidates){
                for(Iterator<Class<?>> it=candidates.iterator();it.hasNext();
                        ){
                    final Class<?> intf2=it.next();
                    if(intf!=intf2&&intf2.isAssignableFrom(intf)){
                        it.remove();
                        continue reduce;
                    }
                }
            }
            final String msg=
                    "Class "+resourceClass.getName()+" implements more than "+
                            "one MXBean interface: "+candidates;
            throw new IllegalArgumentException(msg);
        }
        if(candidates.iterator().hasNext()){
            return Util.cast(candidates.iterator().next());
        }else{
            final String msg=
                    "Class "+resourceClass.getName()+
                            " is not a JMX compliant MXBean";
            throw new IllegalArgumentException(msg);
        }
    }

    private static Set<Class<?>> transitiveInterfaces(Class<?> c){
        Set<Class<?>> set=newSet();
        transitiveInterfaces(c,set);
        return set;
    }

    private static void transitiveInterfaces(Class<?> c,Set<Class<?>> intfs){
        if(c==null)
            return;
        if(c.isInterface())
            intfs.add(c);
        transitiveInterfaces(c.getSuperclass(),intfs);
        for(Class<?> sup : c.getInterfaces())
            transitiveInterfaces(sup,intfs);
    }

    @Override
    MBeanIntrospector<ConvertingMethod> getMBeanIntrospector(){
        return MXBeanIntrospector.getInstance();
    }

    @Override
    Object getCookie(){
        return mxbeanLookup;
    }

    @Override
    public void register(MBeanServer server,ObjectName name)
            throws InstanceAlreadyExistsException{
        if(name==null)
            throw new IllegalArgumentException("Null object name");
        // eventually we could have some logic to supply a default name
        synchronized(lock){
            this.mxbeanLookup=MXBeanLookup.lookupFor(server);
            this.mxbeanLookup.addReference(name,getResource());
            this.objectName=name;
        }
    }

    @Override
    public void unregister(){
        synchronized(lock){
            if(mxbeanLookup!=null){
                if(mxbeanLookup.removeReference(objectName,getResource()))
                    objectName=null;
            }
        }
    }
}
