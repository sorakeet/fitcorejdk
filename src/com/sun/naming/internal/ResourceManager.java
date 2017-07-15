/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.naming.internal;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public final class ResourceManager{
    private static final String PROVIDER_RESOURCE_FILE_NAME=
            "jndiprovider.properties";
    private static final String APP_RESOURCE_FILE_NAME="jndi.properties";
    private static final String JRELIB_PROPERTY_FILE_NAME="jndi.properties";
    private static final String DISABLE_APP_RESOURCE_FILES=
            "com.sun.naming.disable.app.resource.files";
    private static final String[] listProperties={
            Context.OBJECT_FACTORIES,
            Context.URL_PKG_PREFIXES,
            Context.STATE_FACTORIES,
            // The following shouldn't create a runtime dependence on ldap package.
            javax.naming.ldap.LdapContext.CONTROL_FACTORIES
    };
    private static final VersionHelper helper=
            VersionHelper.getVersionHelper();
    // WeakHashMap<Class | ClassLoader, Hashtable>
    private static final WeakHashMap<Object,Hashtable<? super String,Object>>
            propertiesCache=new WeakHashMap<>(11);
    private static final
    WeakHashMap<ClassLoader,Map<String,List<NamedWeakReference<Object>>>>
            factoryCache=new WeakHashMap<>(11);
    private static final
    WeakHashMap<ClassLoader,Map<String,WeakReference<Object>>>
            urlFactoryCache=new WeakHashMap<>(11);
    private static final WeakReference<Object> NO_FACTORY=
            new WeakReference<>(null);

    // There should be no instances of this class.
    private ResourceManager(){
    }

    @SuppressWarnings("unchecked")
    public static Hashtable<?,?> getInitialEnvironment(
            Hashtable<?,?> env)
            throws NamingException{
        String[] props=VersionHelper.PROPS;   // system/applet properties
        if(env==null){
            env=new Hashtable<>(11);
        }
        Object applet=env.get(Context.APPLET);
        // Merge property values from env param, applet params, and system
        // properties.  The first value wins:  there's no concatenation of
        // colon-separated lists.
        // Read system properties by first trying System.getProperties(),
        // and then trying System.getProperty() if that fails.  The former
        // is more efficient due to fewer permission checks.
        //
        String[] jndiSysProps=helper.getJndiProperties();
        for(int i=0;i<props.length;i++){
            Object val=env.get(props[i]);
            if(val==null){
                if(applet!=null){
                    val=AppletParameter.get(applet,props[i]);
                }
                if(val==null){
                    // Read system property.
                    val=(jndiSysProps!=null)
                            ?jndiSysProps[i]
                            :helper.getJndiProperty(i);
                }
                if(val!=null){
                    ((Hashtable<String,Object>)env).put(props[i],val);
                }
            }
        }
        // Return without merging if application resource files lookup
        // is disabled.
        String disableAppRes=(String)env.get(DISABLE_APP_RESOURCE_FILES);
        if(disableAppRes!=null&&disableAppRes.equalsIgnoreCase("true")){
            return env;
        }
        // Merge the above with the values read from all application
        // resource files.  Colon-separated lists are concatenated.
        mergeTables((Hashtable<Object,Object>)env,getApplicationResources());
        return env;
    }
    // ---------- Public methods ----------

    private static Hashtable<? super String,Object> getApplicationResources()
            throws NamingException{
        ClassLoader cl=helper.getContextClassLoader();
        synchronized(propertiesCache){
            Hashtable<? super String,Object> result=propertiesCache.get(cl);
            if(result!=null){
                return result;
            }
            try{
                NamingEnumeration<InputStream> resources=
                        helper.getResources(cl,APP_RESOURCE_FILE_NAME);
                try{
                    while(resources.hasMore()){
                        Properties props=new Properties();
                        InputStream istream=resources.next();
                        try{
                            props.load(istream);
                        }finally{
                            istream.close();
                        }
                        if(result==null){
                            result=props;
                        }else{
                            mergeTables(result,props);
                        }
                    }
                }finally{
                    while(resources.hasMore()){
                        resources.next().close();
                    }
                }
                // Merge in properties from file in <java.home>/lib.
                InputStream istream=
                        helper.getJavaHomeLibStream(JRELIB_PROPERTY_FILE_NAME);
                if(istream!=null){
                    try{
                        Properties props=new Properties();
                        props.load(istream);
                        if(result==null){
                            result=props;
                        }else{
                            mergeTables(result,props);
                        }
                    }finally{
                        istream.close();
                    }
                }
            }catch(IOException e){
                NamingException ne=new ConfigurationException(
                        "Error reading application resource file");
                ne.setRootCause(e);
                throw ne;
            }
            if(result==null){
                result=new Hashtable<>(11);
            }
            propertiesCache.put(cl,result);
            return result;
        }
    }

    private static void mergeTables(Hashtable<? super String,Object> props1,
                                    Hashtable<? super String,Object> props2){
        for(Object key : props2.keySet()){
            String prop=(String)key;
            Object val1=props1.get(prop);
            if(val1==null){
                props1.put(prop,props2.get(prop));
            }else if(isListProperty(prop)){
                String val2=(String)props2.get(prop);
                props1.put(prop,((String)val1)+":"+val2);
            }
        }
    }

    private static boolean isListProperty(String prop){
        prop=prop.intern();
        for(int i=0;i<listProperties.length;i++){
            if(prop==listProperties[i]){
                return true;
            }
        }
        return false;
    }

    public static FactoryEnumeration getFactories(String propName,
                                                  Hashtable<?,?> env,Context ctx) throws NamingException{
        String facProp=getProperty(propName,env,ctx,true);
        if(facProp==null)
            return null;  // no classes specified; return null
        // Cache is based on context class loader and property val
        ClassLoader loader=helper.getContextClassLoader();
        Map<String,List<NamedWeakReference<Object>>> perLoaderCache=null;
        synchronized(factoryCache){
            perLoaderCache=factoryCache.get(loader);
            if(perLoaderCache==null){
                perLoaderCache=new HashMap<>(11);
                factoryCache.put(loader,perLoaderCache);
            }
        }
        synchronized(perLoaderCache){
            List<NamedWeakReference<Object>> factories=
                    perLoaderCache.get(facProp);
            if(factories!=null){
                // Cached list
                return factories.size()==0?null
                        :new FactoryEnumeration(factories,loader);
            }else{
                // Populate list with classes named in facProp; skipping
                // those that we cannot load
                StringTokenizer parser=new StringTokenizer(facProp,":");
                factories=new ArrayList<>(5);
                while(parser.hasMoreTokens()){
                    try{
                        // System.out.println("loading");
                        String className=parser.nextToken();
                        Class<?> c=helper.loadClass(className,loader);
                        factories.add(new NamedWeakReference<Object>(c,className));
                    }catch(Exception e){
                        // ignore ClassNotFoundException, IllegalArgumentException
                    }
                }
                // System.out.println("adding to cache: " + factories);
                perLoaderCache.put(facProp,factories);
                return new FactoryEnumeration(factories,loader);
            }
        }
    }
    // ---------- Private methods ----------

    public static String getProperty(String propName,Hashtable<?,?> env,
                                     Context ctx,boolean concat)
            throws NamingException{
        String val1=(env!=null)?(String)env.get(propName):null;
        if((ctx==null)||
                ((val1!=null)&&!concat)){
            return val1;
        }
        String val2=(String)getProviderResource(ctx).get(propName);
        if(val1==null){
            return val2;
        }else if((val2==null)||!concat){
            return val1;
        }else{
            return (val1+":"+val2);
        }
    }

    private static Hashtable<? super String,Object>
    getProviderResource(Object obj)
            throws NamingException{
        if(obj==null){
            return (new Hashtable<>(1));
        }
        synchronized(propertiesCache){
            Class<?> c=obj.getClass();
            Hashtable<? super String,Object> props=
                    propertiesCache.get(c);
            if(props!=null){
                return props;
            }
            props=new Properties();
            InputStream istream=
                    helper.getResourceAsStream(c,PROVIDER_RESOURCE_FILE_NAME);
            if(istream!=null){
                try{
                    ((Properties)props).load(istream);
                }catch(IOException e){
                    NamingException ne=new ConfigurationException(
                            "Error reading provider resource file for "+c);
                    ne.setRootCause(e);
                    throw ne;
                }
            }
            propertiesCache.put(c,props);
            return props;
        }
    }

    public static Object getFactory(String propName,Hashtable<?,?> env,
                                    Context ctx,String classSuffix,String defaultPkgPrefix)
            throws NamingException{
        // Merge property with provider property and supplied default
        String facProp=getProperty(propName,env,ctx,true);
        if(facProp!=null)
            facProp+=(":"+defaultPkgPrefix);
        else
            facProp=defaultPkgPrefix;
        // Cache factory based on context class loader, class name, and
        // property val
        ClassLoader loader=helper.getContextClassLoader();
        String key=classSuffix+" "+facProp;
        Map<String,WeakReference<Object>> perLoaderCache=null;
        synchronized(urlFactoryCache){
            perLoaderCache=urlFactoryCache.get(loader);
            if(perLoaderCache==null){
                perLoaderCache=new HashMap<>(11);
                urlFactoryCache.put(loader,perLoaderCache);
            }
        }
        synchronized(perLoaderCache){
            Object factory=null;
            WeakReference<Object> factoryRef=perLoaderCache.get(key);
            if(factoryRef==NO_FACTORY){
                return null;
            }else if(factoryRef!=null){
                factory=factoryRef.get();
                if(factory!=null){  // check if weak ref has been cleared
                    return factory;
                }
            }
            // Not cached; find first factory and cache
            StringTokenizer parser=new StringTokenizer(facProp,":");
            String className;
            while(factory==null&&parser.hasMoreTokens()){
                className=parser.nextToken()+classSuffix;
                try{
                    // System.out.println("loading " + className);
                    factory=helper.loadClass(className,loader).newInstance();
                }catch(InstantiationException e){
                    NamingException ne=
                            new NamingException("Cannot instantiate "+className);
                    ne.setRootCause(e);
                    throw ne;
                }catch(IllegalAccessException e){
                    NamingException ne=
                            new NamingException("Cannot access "+className);
                    ne.setRootCause(e);
                    throw ne;
                }catch(Exception e){
                    // ignore ClassNotFoundException, IllegalArgumentException,
                    // etc.
                }
            }
            // Cache it.
            perLoaderCache.put(key,(factory!=null)
                    ?new WeakReference<>(factory)
                    :NO_FACTORY);
            return factory;
        }
    }

    private static class AppletParameter{
        private static final Class<?> clazz=getClass("java.applet.Applet");
        private static final Method getMethod=
                getMethod(clazz,"getParameter",String.class);

        private static Class<?> getClass(String name){
            try{
                return Class.forName(name,true,null);
            }catch(ClassNotFoundException e){
                return null;
            }
        }

        private static Method getMethod(Class<?> clazz,
                                        String name,
                                        Class<?>... paramTypes){
            if(clazz!=null){
                try{
                    return clazz.getMethod(name,paramTypes);
                }catch(NoSuchMethodException e){
                    throw new AssertionError(e);
                }
            }else{
                return null;
            }
        }

        static Object get(Object applet,String name){
            // if clazz is null then applet cannot be an Applet.
            if(clazz==null||!clazz.isInstance(applet))
                throw new ClassCastException(applet.getClass().getName());
            try{
                return getMethod.invoke(applet,name);
            }catch(InvocationTargetException|
                    IllegalAccessException e){
                throw new AssertionError(e);
            }
        }
    }
}
