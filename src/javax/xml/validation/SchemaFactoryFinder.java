/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.validation;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

class SchemaFactoryFinder{
    private static final SecuritySupport ss=new SecuritySupport();
    private static final String DEFAULT_PACKAGE="com.sun.org.apache.xerces.internal";
    private static final Properties cacheProps=new Properties();
    private static final Class<SchemaFactory> SERVICE_CLASS=SchemaFactory.class;
    private static boolean debug=false;
    private static volatile boolean firstTime=true;

    static{
        // Use try/catch block to support applets
        try{
            debug=ss.getSystemProperty("jaxp.debug")!=null;
        }catch(Exception unused){
            debug=false;
        }
    }

    private final ClassLoader classLoader;

    public SchemaFactoryFinder(ClassLoader loader){
        this.classLoader=loader;
        if(debug){
            debugDisplayClassLoader();
        }
    }

    private void debugDisplayClassLoader(){
        try{
            if(classLoader==ss.getContextClassLoader()){
                debugPrintln("using thread context class loader ("+classLoader+") for search");
                return;
            }
        }catch(Throwable unused){
            // getContextClassLoader() undefined in JDK1.1
        }
        if(classLoader==ClassLoader.getSystemClassLoader()){
            debugPrintln("using system class loader ("+classLoader+") for search");
            return;
        }
        debugPrintln("using class loader ("+classLoader+") for search");
    }

    private static void debugPrintln(String msg){
        if(debug){
            System.err.println("JAXP: "+msg);
        }
    }

    public SchemaFactory newFactory(String schemaLanguage){
        if(schemaLanguage==null){
            throw new NullPointerException();
        }
        SchemaFactory f=_newFactory(schemaLanguage);
        if(f!=null){
            debugPrintln("factory '"+f.getClass().getName()+"' was found for "+schemaLanguage);
        }else{
            debugPrintln("unable to find a factory for "+schemaLanguage);
        }
        return f;
    }

    private SchemaFactory _newFactory(String schemaLanguage){
        SchemaFactory sf;
        String propertyName=SERVICE_CLASS.getName()+":"+schemaLanguage;
        // system property look up
        try{
            debugPrintln("Looking up system property '"+propertyName+"'");
            String r=ss.getSystemProperty(propertyName);
            if(r!=null){
                debugPrintln("The value is '"+r+"'");
                sf=createInstance(r,true);
                if(sf!=null) return sf;
            }else
                debugPrintln("The property is undefined.");
        }catch(Throwable t){
            if(debug){
                debugPrintln("failed to look up system property '"+propertyName+"'");
                t.printStackTrace();
            }
        }
        String javah=ss.getSystemProperty("java.home");
        String configFile=javah+File.separator+
                "lib"+File.separator+"jaxp.properties";
        // try to read from $java.home/lib/jaxp.properties
        try{
            if(firstTime){
                synchronized(cacheProps){
                    if(firstTime){
                        File f=new File(configFile);
                        firstTime=false;
                        if(ss.doesFileExist(f)){
                            debugPrintln("Read properties file "+f);
                            cacheProps.load(ss.getFileInputStream(f));
                        }
                    }
                }
            }
            final String factoryClassName=cacheProps.getProperty(propertyName);
            debugPrintln("found "+factoryClassName+" in $java.home/jaxp.properties");
            if(factoryClassName!=null){
                sf=createInstance(factoryClassName,true);
                if(sf!=null){
                    return sf;
                }
            }
        }catch(Exception ex){
            if(debug){
                ex.printStackTrace();
            }
        }
        // Try with ServiceLoader
        final SchemaFactory factoryImpl=findServiceProvider(schemaLanguage);
        // The following assertion should always be true.
        // Uncomment it, recompile, and run with -ea in case of doubts:
        // assert factoryImpl == null || factoryImpl.isSchemaLanguageSupported(schemaLanguage);
        if(factoryImpl!=null){
            return factoryImpl;
        }
        // platform default
        if(schemaLanguage.equals("http://www.w3.org/2001/XMLSchema")){
            debugPrintln("attempting to use the platform default XML Schema validator");
            return createInstance("com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory",true);
        }
        debugPrintln("all things were tried, but none was found. bailing out.");
        return null;
    }

    SchemaFactory createInstance(String className,boolean useServicesMechanism){
        SchemaFactory schemaFactory=null;
        debugPrintln("createInstance("+className+")");
        // get Class from className
        Class<?> clazz=createClass(className);
        if(clazz==null){
            debugPrintln("failed to getClass("+className+")");
            return null;
        }
        debugPrintln("loaded "+className+" from "+which(clazz));
        // instantiate Class as a SchemaFactory
        try{
            if(!SchemaFactory.class.isAssignableFrom(clazz)){
                throw new ClassCastException(clazz.getName()
                        +" cannot be cast to "+SchemaFactory.class);
            }
            if(!useServicesMechanism){
                schemaFactory=newInstanceNoServiceLoader(clazz);
            }
            if(schemaFactory==null){
                schemaFactory=(SchemaFactory)clazz.newInstance();
            }
        }catch(ClassCastException classCastException){
            debugPrintln("could not instantiate "+clazz.getName());
            if(debug){
                classCastException.printStackTrace();
            }
            return null;
        }catch(IllegalAccessException illegalAccessException){
            debugPrintln("could not instantiate "+clazz.getName());
            if(debug){
                illegalAccessException.printStackTrace();
            }
            return null;
        }catch(InstantiationException instantiationException){
            debugPrintln("could not instantiate "+clazz.getName());
            if(debug){
                instantiationException.printStackTrace();
            }
            return null;
        }
        return schemaFactory;
    }

    private Class<?> createClass(String className){
        Class<?> clazz;
        // make sure we have access to restricted packages
        boolean internal=false;
        if(System.getSecurityManager()!=null){
            if(className!=null&&className.startsWith(DEFAULT_PACKAGE)){
                internal=true;
            }
        }
        try{
            if(classLoader!=null&&!internal){
                clazz=Class.forName(className,false,classLoader);
            }else{
                clazz=Class.forName(className);
            }
        }catch(Throwable t){
            if(debug){
                t.printStackTrace();
            }
            return null;
        }
        return clazz;
    }

    private static SchemaFactory newInstanceNoServiceLoader(
            Class<?> providerClass
    ){
        // Retain maximum compatibility if no security manager.
        if(System.getSecurityManager()==null){
            return null;
        }
        try{
            final Method creationMethod=
                    providerClass.getDeclaredMethod(
                            "newXMLSchemaFactoryNoServiceLoader"
                    );
            final int modifiers=creationMethod.getModifiers();
            // Do not call the method if it's not public static.
            if(!Modifier.isStatic(modifiers)||!Modifier.isPublic(modifiers)){
                return null;
            }
            // Only calls "newXMLSchemaFactoryNoServiceLoader" if it's
            // declared to return an instance of SchemaFactory.
            final Class<?> returnType=creationMethod.getReturnType();
            if(SERVICE_CLASS.isAssignableFrom(returnType)){
                return SERVICE_CLASS.cast(creationMethod.invoke(null,(Object[])null));
            }else{
                // Should not happen since
                // XMLSchemaFactory.newXMLSchemaFactoryNoServiceLoader is
                // declared to return XMLSchemaFactory.
                throw new ClassCastException(returnType
                        +" cannot be cast to "+SERVICE_CLASS);
            }
        }catch(ClassCastException e){
            throw new SchemaFactoryConfigurationError(e.getMessage(),e);
        }catch(NoSuchMethodException exc){
            return null;
        }catch(Exception exc){
            return null;
        }
    }

    private static String which(Class<?> clazz){
        return which(clazz.getName(),clazz.getClassLoader());
    }

    private static String which(String classname,ClassLoader loader){
        String classnameAsResource=classname.replace('.','/')+".class";
        if(loader==null) loader=ClassLoader.getSystemClassLoader();
        //URL it = loader.getResource(classnameAsResource);
        URL it=ss.getResourceAsURL(loader,classnameAsResource);
        if(it!=null){
            return it.toString();
        }else{
            return null;
        }
    }

    private SchemaFactory findServiceProvider(final String schemaLanguage){
        assert schemaLanguage!=null;
        // store current context.
        final AccessControlContext acc=AccessController.getContext();
        try{
            return AccessController.doPrivileged(new PrivilegedAction<SchemaFactory>(){
                public SchemaFactory run(){
                    final ServiceLoader<SchemaFactory> loader=
                            ServiceLoader.load(SERVICE_CLASS);
                    for(SchemaFactory factory : loader){
                        // restore initial context to call
                        // factory.isSchemaLanguageSupported
                        if(isSchemaLanguageSupportedBy(factory,schemaLanguage,acc)){
                            return factory;
                        }
                    }
                    return null; // no factory found.
                }
            });
        }catch(ServiceConfigurationError error){
            throw new SchemaFactoryConfigurationError(
                    "Provider for "+SERVICE_CLASS+" cannot be created",error);
        }
    }

    // Call isSchemaLanguageSupported with initial context.
    private boolean isSchemaLanguageSupportedBy(final SchemaFactory factory,
                                                final String schemaLanguage,
                                                AccessControlContext acc){
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>(){
            public Boolean run(){
                return factory.isSchemaLanguageSupported(schemaLanguage);
            }
        },acc);
    }

    SchemaFactory createInstance(String className){
        return createInstance(className,false);
    }
}
