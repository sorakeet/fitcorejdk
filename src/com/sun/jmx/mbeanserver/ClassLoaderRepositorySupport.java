/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.mbeanserver;

import sun.reflect.misc.ReflectUtil;

import javax.management.MBeanPermission;
import javax.management.ObjectName;
import javax.management.loading.PrivateClassLoader;
import java.security.Permission;
import java.util.*;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.MBEANSERVER_LOGGER;

final class ClassLoaderRepositorySupport
        implements ModifiableClassLoaderRepository{
    private static final LoaderEntry[] EMPTY_LOADER_ARRAY=new LoaderEntry[0];
    private final Map<String,List<ClassLoader>> search=
            new Hashtable<String,List<ClassLoader>>(10);
    private final Map<ObjectName,ClassLoader> loadersWithNames=
            new Hashtable<ObjectName,ClassLoader>(10);
    private LoaderEntry[] loaders=EMPTY_LOADER_ARRAY;

    // from javax.management.loading.DefaultLoaderRepository
    public final Class<?> loadClass(String className)
            throws ClassNotFoundException{
        return loadClass(loaders,className,null,null);
    }

    // from javax.management.loading.DefaultLoaderRepository
    public final Class<?> loadClassWithout(ClassLoader without,String className)
            throws ClassNotFoundException{
        if(MBEANSERVER_LOGGER.isLoggable(Level.FINER)){
            MBEANSERVER_LOGGER.logp(Level.FINER,
                    ClassLoaderRepositorySupport.class.getName(),
                    "loadClassWithout",className+" without "+without);
        }
        // without is null => just behave as loadClass
        //
        if(without==null)
            return loadClass(loaders,className,null,null);
        // We must try to load the class without the given loader.
        //
        startValidSearch(without,className);
        try{
            return loadClass(loaders,className,without,null);
        }finally{
            stopValidSearch(without,className);
        }
    }

    public final Class<?> loadClassBefore(ClassLoader stop,String className)
            throws ClassNotFoundException{
        if(MBEANSERVER_LOGGER.isLoggable(Level.FINER)){
            MBEANSERVER_LOGGER.logp(Level.FINER,
                    ClassLoaderRepositorySupport.class.getName(),
                    "loadClassBefore",className+" before "+stop);
        }
        if(stop==null)
            return loadClass(loaders,className,null,null);
        startValidSearch(stop,className);
        try{
            return loadClass(loaders,className,null,stop);
        }finally{
            stopValidSearch(stop,className);
        }
    }

    private synchronized void startValidSearch(ClassLoader aloader,
                                               String className)
            throws ClassNotFoundException{
        // Check if we have such a current search
        //
        List<ClassLoader> excluded=search.get(className);
        if((excluded!=null)&&(excluded.contains(aloader))){
            if(MBEANSERVER_LOGGER.isLoggable(Level.FINER)){
                MBEANSERVER_LOGGER.logp(Level.FINER,
                        ClassLoaderRepositorySupport.class.getName(),
                        "startValidSearch","Already requested loader = "+
                                aloader+" class = "+className);
            }
            throw new ClassNotFoundException(className);
        }
        // Add an entry
        //
        if(excluded==null){
            excluded=new ArrayList<ClassLoader>(1);
            search.put(className,excluded);
        }
        excluded.add(aloader);
        if(MBEANSERVER_LOGGER.isLoggable(Level.FINER)){
            MBEANSERVER_LOGGER.logp(Level.FINER,
                    ClassLoaderRepositorySupport.class.getName(),
                    "startValidSearch",
                    "loader = "+aloader+" class = "+className);
        }
    }

    private synchronized void stopValidSearch(ClassLoader aloader,
                                              String className){
        // Retrieve the search.
        //
        List<ClassLoader> excluded=search.get(className);
        if(excluded!=null){
            excluded.remove(aloader);
            if(MBEANSERVER_LOGGER.isLoggable(Level.FINER)){
                MBEANSERVER_LOGGER.logp(Level.FINER,
                        ClassLoaderRepositorySupport.class.getName(),
                        "stopValidSearch",
                        "loader = "+aloader+" class = "+className);
            }
        }
    }

    private Class<?> loadClass(final LoaderEntry list[],
                               final String className,
                               final ClassLoader without,
                               final ClassLoader stop)
            throws ClassNotFoundException{
        ReflectUtil.checkPackageAccess(className);
        final int size=list.length;
        for(int i=0;i<size;i++){
            try{
                final ClassLoader cl=list[i].loader;
                if(cl==null) // bootstrap class loader
                    return Class.forName(className,false,null);
                if(cl==without)
                    continue;
                if(cl==stop)
                    break;
                if(MBEANSERVER_LOGGER.isLoggable(Level.FINER)){
                    MBEANSERVER_LOGGER.logp(Level.FINER,
                            ClassLoaderRepositorySupport.class.getName(),
                            "loadClass","Trying loader = "+cl);
                }
                /** We used to have a special case for "instanceof
                 MLet" here, where we invoked the method
                 loadClass(className, null) to prevent infinite
                 recursion.  But the rule whereby the MLet only
                 consults loaders that precede it in the CLR (via
                 loadClassBefore) means that the recursion can't
                 happen, and the test here caused some legitimate
                 classloading to fail.  For example, if you have
                 dependencies C->D->E with loaders {E D C} in the
                 CLR in that order, you would expect to be able to
                 load C.  The problem is that while resolving D, CLR
                 delegation is disabled, so it can't find E.  */
                return Class.forName(className,false,cl);
            }catch(ClassNotFoundException e){
                // OK: continue with next class
            }
        }
        throw new ClassNotFoundException(className);
    }

    public final void addClassLoader(ClassLoader loader){
        add(null,loader);
    }

    private synchronized boolean add(ObjectName name,ClassLoader cl){
        List<LoaderEntry> l=
                new ArrayList<LoaderEntry>(Arrays.asList(loaders));
        l.add(new LoaderEntry(name,cl));
        loaders=l.toArray(EMPTY_LOADER_ARRAY);
        return true;
    }

    public final void removeClassLoader(ClassLoader loader){
        remove(null,loader);
    }

    private synchronized boolean remove(ObjectName name,ClassLoader cl){
        final int size=loaders.length;
        for(int i=0;i<size;i++){
            LoaderEntry entry=loaders[i];
            boolean match=
                    (name==null)?
                            cl==entry.loader:
                            name.equals(entry.name);
            if(match){
                LoaderEntry[] newloaders=new LoaderEntry[size-1];
                System.arraycopy(loaders,0,newloaders,0,i);
                System.arraycopy(loaders,i+1,newloaders,i,
                        size-1-i);
                loaders=newloaders;
                return true;
            }
        }
        return false;
    }

    public final synchronized void addClassLoader(ObjectName name,
                                                  ClassLoader loader){
        loadersWithNames.put(name,loader);
        if(!(loader instanceof PrivateClassLoader))
            add(name,loader);
    }

    public final synchronized void removeClassLoader(ObjectName name){
        ClassLoader loader=loadersWithNames.remove(name);
        if(!(loader instanceof PrivateClassLoader))
            remove(name,loader);
    }

    public final ClassLoader getClassLoader(ObjectName name){
        ClassLoader instance=loadersWithNames.get(name);
        if(instance!=null){
            SecurityManager sm=System.getSecurityManager();
            if(sm!=null){
                Permission perm=
                        new MBeanPermission(instance.getClass().getName(),
                                null,
                                name,
                                "getClassLoader");
                sm.checkPermission(perm);
            }
        }
        return instance;
    }

    private static class LoaderEntry{
        ObjectName name; // can be null
        ClassLoader loader;

        LoaderEntry(ObjectName name,ClassLoader loader){
            this.name=name;
            this.loader=loader;
        }
    }
}
