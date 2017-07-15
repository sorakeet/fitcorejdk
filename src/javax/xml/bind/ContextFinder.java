/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.xml.bind.JAXBContext.JAXB_CONTEXT_FACTORY;

class ContextFinder{
    private static final Logger logger;
    private static final String PLATFORM_DEFAULT_FACTORY_CLASS="com.sun.xml.internal.bind.v2.ContextFactory";

    static{
        logger=Logger.getLogger("javax.xml.bind");
        try{
            if(AccessController.doPrivileged(new GetPropertyAction("jaxb.debug"))!=null){
                // disconnect the logger from a bigger framework (if any)
                // and take the matters into our own hands
                logger.setUseParentHandlers(false);
                logger.setLevel(Level.ALL);
                ConsoleHandler handler=new ConsoleHandler();
                handler.setLevel(Level.ALL);
                logger.addHandler(handler);
            }else{
                // don't change the setting of this logger
                // to honor what other frameworks
                // have done on configurations.
            }
        }catch(Throwable t){
            // just to be extra safe. in particular System.getProperty may throw
            // SecurityException.
        }
    }

    private static void handleInvocationTargetException(InvocationTargetException x) throws JAXBException{
        Throwable t=x.getTargetException();
        if(t!=null){
            if(t instanceof JAXBException)
                // one of our exceptions, just re-throw
                throw (JAXBException)t;
            if(t instanceof RuntimeException)
                // avoid wrapping exceptions unnecessarily
                throw (RuntimeException)t;
            if(t instanceof Error)
                throw (Error)t;
        }
    }

    private static JAXBException handleClassCastException(Class originalType,Class targetType){
        final URL targetTypeURL=which(targetType);
        return new JAXBException(Messages.format(Messages.ILLEGAL_CAST,
                // we don't care where the impl class is, we want to know where JAXBContext lives in the impl
                // class' ClassLoader
                getClassClassLoader(originalType).getResource("javax/xml/bind/JAXBContext.class"),
                targetTypeURL));
    }

    static JAXBContext newInstance(String contextPath,
                                   String className,
                                   ClassLoader classLoader,
                                   Map properties)
            throws JAXBException{
        try{
            Class spFactory=safeLoadClass(className,classLoader);
            return newInstance(contextPath,spFactory,classLoader,properties);
        }catch(ClassNotFoundException x){
            throw new JAXBException(
                    Messages.format(Messages.PROVIDER_NOT_FOUND,className),
                    x);
        }catch(RuntimeException x){
            // avoid wrapping RuntimeException to JAXBException,
            // because it indicates a bug in this code.
            throw x;
        }catch(Exception x){
            // can't catch JAXBException because the method is hidden behind
            // reflection.  Root element collisions detected in the call to
            // createContext() are reported as JAXBExceptions - just re-throw it
            // some other type of exception - just wrap it
            throw new JAXBException(
                    Messages.format(Messages.COULD_NOT_INSTANTIATE,className,x),
                    x);
        }
    }

    static JAXBContext newInstance(String contextPath,
                                   Class spFactory,
                                   ClassLoader classLoader,
                                   Map properties)
            throws JAXBException{
        try{
            /**
             * javax.xml.bind.context.factory points to a class which has a
             * static method called 'createContext' that
             * returns a javax.xml.JAXBContext.
             */
            Object context=null;
            // first check the method that takes Map as the third parameter.
            // this is added in 2.0.
            try{
                Method m=spFactory.getMethod("createContext",String.class,ClassLoader.class,Map.class);
                // any failure in invoking this method would be considered fatal
                context=m.invoke(null,contextPath,classLoader,properties);
            }catch(NoSuchMethodException e){
                // it's not an error for the provider not to have this method.
            }
            if(context==null){
                // try the old method that doesn't take properties. compatible with 1.0.
                // it is an error for an implementation not to have both forms of the createContext method.
                Method m=spFactory.getMethod("createContext",String.class,ClassLoader.class);
                // any failure in invoking this method would be considered fatal
                context=m.invoke(null,contextPath,classLoader);
            }
            if(!(context instanceof JAXBContext)){
                // the cast would fail, so generate an exception with a nice message
                throw handleClassCastException(context.getClass(),JAXBContext.class);
            }
            return (JAXBContext)context;
        }catch(InvocationTargetException x){
            handleInvocationTargetException(x);
            // for other exceptions, wrap the internal target exception
            // with a JAXBException
            Throwable e=x;
            if(x.getTargetException()!=null)
                e=x.getTargetException();
            throw new JAXBException(Messages.format(Messages.COULD_NOT_INSTANTIATE,spFactory,e),e);
        }catch(RuntimeException x){
            // avoid wrapping RuntimeException to JAXBException,
            // because it indicates a bug in this code.
            throw x;
        }catch(Exception x){
            // can't catch JAXBException because the method is hidden behind
            // reflection.  Root element collisions detected in the call to
            // createContext() are reported as JAXBExceptions - just re-throw it
            // some other type of exception - just wrap it
            throw new JAXBException(
                    Messages.format(Messages.COULD_NOT_INSTANTIATE,spFactory,x),
                    x);
        }
    }

    static JAXBContext newInstance(
            Class[] classes,
            Map properties,
            String className) throws JAXBException{
        ClassLoader cl=getContextClassLoader();
        Class spi;
        try{
            spi=safeLoadClass(className,cl);
        }catch(ClassNotFoundException e){
            throw new JAXBException(e);
        }
        if(logger.isLoggable(Level.FINE)){
            // extra check to avoid costly which operation if not logged
            logger.log(Level.FINE,"loaded {0} from {1}",new Object[]{className,which(spi)});
        }
        return newInstance(classes,properties,spi);
    }

    static JAXBContext newInstance(Class[] classes,
                                   Map properties,
                                   Class spFactory) throws JAXBException{
        Method m;
        try{
            m=spFactory.getMethod("createContext",Class[].class,Map.class);
        }catch(NoSuchMethodException e){
            throw new JAXBException(e);
        }
        try{
            Object context=m.invoke(null,classes,properties);
            if(!(context instanceof JAXBContext)){
                // the cast would fail, so generate an exception with a nice message
                throw handleClassCastException(context.getClass(),JAXBContext.class);
            }
            return (JAXBContext)context;
        }catch(IllegalAccessException e){
            throw new JAXBException(e);
        }catch(InvocationTargetException e){
            handleInvocationTargetException(e);
            Throwable x=e;
            if(e.getTargetException()!=null)
                x=e.getTargetException();
            throw new JAXBException(x);
        }
    }

    static JAXBContext find(String factoryId,String contextPath,ClassLoader classLoader,Map properties) throws JAXBException{
        // TODO: do we want/need another layer of searching in $java.home/lib/jaxb.properties like JAXP?
        final String jaxbContextFQCN=JAXBContext.class.getName();
        // search context path for jaxb.properties first
        StringBuilder propFileName;
        StringTokenizer packages=new StringTokenizer(contextPath,":");
        String factoryClassName;
        if(!packages.hasMoreTokens())
            // no context is specified
            throw new JAXBException(Messages.format(Messages.NO_PACKAGE_IN_CONTEXTPATH));
        logger.fine("Searching jaxb.properties");
        while(packages.hasMoreTokens()){
            String packageName=packages.nextToken(":").replace('.','/');
            // com.acme.foo - > com/acme/foo/jaxb.properties
            propFileName=new StringBuilder().append(packageName).append("/jaxb.properties");
            Properties props=loadJAXBProperties(classLoader,propFileName.toString());
            if(props!=null){
                if(props.containsKey(factoryId)){
                    factoryClassName=props.getProperty(factoryId);
                    return newInstance(contextPath,factoryClassName,classLoader,properties);
                }else{
                    throw new JAXBException(Messages.format(Messages.MISSING_PROPERTY,packageName,factoryId));
                }
            }
        }
        logger.fine("Searching the system property");
        // search for a system property second (javax.xml.bind.JAXBContext)
        factoryClassName=AccessController.doPrivileged(new GetPropertyAction(JAXBContext.JAXB_CONTEXT_FACTORY));
        if(factoryClassName!=null){
            return newInstance(contextPath,factoryClassName,classLoader,properties);
        }else{ // leave this here to assure compatibility
            factoryClassName=AccessController.doPrivileged(new GetPropertyAction(jaxbContextFQCN));
            if(factoryClassName!=null){
                return newInstance(contextPath,factoryClassName,classLoader,properties);
            }
        }
        // OSGi search
        Class jaxbContext=lookupJaxbContextUsingOsgiServiceLoader();
        if(jaxbContext!=null){
            logger.fine("OSGi environment detected");
            return newInstance(contextPath,jaxbContext,classLoader,properties);
        }
        logger.fine("Searching META-INF/services");
        // search META-INF services next
        BufferedReader r=null;
        try{
            final StringBuilder resource=new StringBuilder().append("META-INF/services/").append(jaxbContextFQCN);
            final InputStream resourceStream=
                    classLoader.getResourceAsStream(resource.toString());
            if(resourceStream!=null){
                r=new BufferedReader(new InputStreamReader(resourceStream,"UTF-8"));
                factoryClassName=r.readLine();
                if(factoryClassName!=null){
                    factoryClassName=factoryClassName.trim();
                }
                r.close();
                return newInstance(contextPath,factoryClassName,classLoader,properties);
            }else{
                logger.log(Level.FINE,"Unable to load:{0}",resource.toString());
            }
        }catch(UnsupportedEncodingException e){
            // should never happen
            throw new JAXBException(e);
        }catch(IOException e){
            throw new JAXBException(e);
        }finally{
            try{
                if(r!=null){
                    r.close();
                }
            }catch(IOException ex){
                Logger.getLogger(ContextFinder.class.getName()).log(Level.SEVERE,null,ex);
            }
        }
        // else no provider found
        logger.fine("Trying to create the platform default provider");
        return newInstance(contextPath,PLATFORM_DEFAULT_FACTORY_CLASS,classLoader,properties);
    }

    static JAXBContext find(Class[] classes,Map properties) throws JAXBException{
        final String jaxbContextFQCN=JAXBContext.class.getName();
        String factoryClassName;
        // search for jaxb.properties in the class loader of each class first
        for(final Class c : classes){
            // this classloader is used only to load jaxb.properties, so doing this should be safe.
            ClassLoader classLoader=getClassClassLoader(c);
            Package pkg=c.getPackage();
            if(pkg==null)
                continue;       // this is possible for primitives, arrays, and classes that are loaded by poorly implemented ClassLoaders
            String packageName=pkg.getName().replace('.','/');
            // TODO: do we want to optimize away searching the same package?  org.Foo, org.Bar, com.Baz
            //       classes from the same package might come from different class loades, so it might be a bad idea
            // TODO: it's easier to look things up from the class
            // c.getResourceAsStream("jaxb.properties");
            // build the resource name and use the property loader code
            String resourceName=packageName+"/jaxb.properties";
            logger.log(Level.FINE,"Trying to locate {0}",resourceName);
            Properties props=loadJAXBProperties(classLoader,resourceName);
            if(props==null){
                logger.fine("  not found");
            }else{
                logger.fine("  found");
                if(props.containsKey(JAXB_CONTEXT_FACTORY)){
                    // trim() seems redundant, but adding to satisfy customer complaint
                    factoryClassName=props.getProperty(JAXB_CONTEXT_FACTORY).trim();
                    return newInstance(classes,properties,factoryClassName);
                }else{
                    throw new JAXBException(Messages.format(Messages.MISSING_PROPERTY,packageName,JAXB_CONTEXT_FACTORY));
                }
            }
        }
        // search for a system property second (javax.xml.bind.JAXBContext)
        logger.log(Level.FINE,"Checking system property {0}",JAXBContext.JAXB_CONTEXT_FACTORY);
        factoryClassName=AccessController.doPrivileged(new GetPropertyAction(JAXBContext.JAXB_CONTEXT_FACTORY));
        if(factoryClassName!=null){
            logger.log(Level.FINE,"  found {0}",factoryClassName);
            return newInstance(classes,properties,factoryClassName);
        }else{ // leave it here for compatibility reasons
            logger.fine("  not found");
            logger.log(Level.FINE,"Checking system property {0}",jaxbContextFQCN);
            factoryClassName=AccessController.doPrivileged(new GetPropertyAction(jaxbContextFQCN));
            if(factoryClassName!=null){
                logger.log(Level.FINE,"  found {0}",factoryClassName);
                return newInstance(classes,properties,factoryClassName);
            }else{
                logger.fine("  not found");
            }
        }
        // OSGi search
        Class jaxbContext=lookupJaxbContextUsingOsgiServiceLoader();
        if(jaxbContext!=null){
            logger.fine("OSGi environment detected");
            return newInstance(classes,properties,jaxbContext);
        }
        // search META-INF services next
        logger.fine("Checking META-INF/services");
        BufferedReader r=null;
        try{
            final String resource=new StringBuilder("META-INF/services/").append(jaxbContextFQCN).toString();
            ClassLoader classLoader=getContextClassLoader();
            URL resourceURL;
            if(classLoader==null)
                resourceURL=ClassLoader.getSystemResource(resource);
            else
                resourceURL=classLoader.getResource(resource);
            if(resourceURL!=null){
                logger.log(Level.FINE,"Reading {0}",resourceURL);
                r=new BufferedReader(new InputStreamReader(resourceURL.openStream(),"UTF-8"));
                factoryClassName=r.readLine();
                if(factoryClassName!=null){
                    factoryClassName=factoryClassName.trim();
                }
                return newInstance(classes,properties,factoryClassName);
            }else{
                logger.log(Level.FINE,"Unable to find: {0}",resource);
            }
        }catch(UnsupportedEncodingException e){
            // should never happen
            throw new JAXBException(e);
        }catch(IOException e){
            throw new JAXBException(e);
        }finally{
            if(r!=null){
                try{
                    r.close();
                }catch(IOException ex){
                    logger.log(Level.FINE,"Unable to close stream",ex);
                }
            }
        }
        // else no provider found
        logger.fine("Trying to create the platform default provider");
        return newInstance(classes,properties,PLATFORM_DEFAULT_FACTORY_CLASS);
    }

    private static Class lookupJaxbContextUsingOsgiServiceLoader(){
        try{
            // Use reflection to avoid having any dependency on ServiceLoader class
            Class target=Class.forName("com.sun.org.glassfish.hk2.osgiresourcelocator.ServiceLoader");
            Method m=target.getMethod("lookupProviderClasses",Class.class);
            Iterator iter=((Iterable)m.invoke(null,JAXBContext.class)).iterator();
            return iter.hasNext()?(Class)iter.next():null;
        }catch(Exception e){
            logger.log(Level.FINE,"Unable to find from OSGi: javax.xml.bind.JAXBContext");
            return null;
        }
    }

    private static Properties loadJAXBProperties(ClassLoader classLoader,
                                                 String propFileName)
            throws JAXBException{
        Properties props=null;
        try{
            URL url;
            if(classLoader==null)
                url=ClassLoader.getSystemResource(propFileName);
            else
                url=classLoader.getResource(propFileName);
            if(url!=null){
                logger.log(Level.FINE,"loading props from {0}",url);
                props=new Properties();
                InputStream is=url.openStream();
                props.load(is);
                is.close();
            }
        }catch(IOException ioe){
            logger.log(Level.FINE,"Unable to load "+propFileName,ioe);
            throw new JAXBException(ioe.toString(),ioe);
        }
        return props;
    }

    static URL which(Class clazz,ClassLoader loader){
        String classnameAsResource=clazz.getName().replace('.','/')+".class";
        if(loader==null){
            loader=getSystemClassLoader();
        }
        return loader.getResource(classnameAsResource);
    }

    static URL which(Class clazz){
        return which(clazz,getClassClassLoader(clazz));
    }

    private static Class safeLoadClass(String className,ClassLoader classLoader) throws ClassNotFoundException{
        logger.log(Level.FINE,"Trying to load {0}",className);
        try{
            // make sure that the current thread has an access to the package of the given name.
            SecurityManager s=System.getSecurityManager();
            if(s!=null){
                int i=className.lastIndexOf('.');
                if(i!=-1){
                    s.checkPackageAccess(className.substring(0,i));
                }
            }
            if(classLoader==null){
                return Class.forName(className);
            }else{
                return classLoader.loadClass(className);
            }
        }catch(SecurityException se){
            // anyone can access the platform default factory class without permission
            if(PLATFORM_DEFAULT_FACTORY_CLASS.equals(className)){
                return Class.forName(className);
            }
            throw se;
        }
    }

    private static ClassLoader getContextClassLoader(){
        if(System.getSecurityManager()==null){
            return Thread.currentThread().getContextClassLoader();
        }else{
            return (ClassLoader)AccessController.doPrivileged(
                    new java.security.PrivilegedAction(){
                        public Object run(){
                            return Thread.currentThread().getContextClassLoader();
                        }
                    });
        }
    }

    private static ClassLoader getClassClassLoader(final Class c){
        if(System.getSecurityManager()==null){
            return c.getClassLoader();
        }else{
            return (ClassLoader)AccessController.doPrivileged(
                    new java.security.PrivilegedAction(){
                        public Object run(){
                            return c.getClassLoader();
                        }
                    });
        }
    }

    private static ClassLoader getSystemClassLoader(){
        if(System.getSecurityManager()==null){
            return ClassLoader.getSystemClassLoader();
        }else{
            return (ClassLoader)AccessController.doPrivileged(
                    new java.security.PrivilegedAction(){
                        public Object run(){
                            return ClassLoader.getSystemClassLoader();
                        }
                    });
        }
    }
}
