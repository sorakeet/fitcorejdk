/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2005 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.utils;

public final class ObjectFactory{
    //
    // Constants
    //
    private static final String JAXP_INTERNAL="com.sun.org.apache";
    private static final String STAX_INTERNAL="com.sun.xml.internal";
    private static final boolean DEBUG=isDebugEnabled();
    //
    // Private static methods
    //

    private static boolean isDebugEnabled(){
        try{
            String val=SecuritySupport.getSystemProperty("xerces.debug");
            // Allow simply setting the prop to turn on debug
            return (val!=null&&(!"false".equals(val)));
        }catch(SecurityException se){
        }
        return false;
    } // isDebugEnabled()

    public static Object newInstance(String className,boolean doFallback)
            throws ConfigurationError{
        if(System.getSecurityManager()!=null){
            return newInstance(className,null,doFallback);
        }else{
            return newInstance(className,
                    findClassLoader(),doFallback);
        }
    }

    public static ClassLoader findClassLoader()
            throws ConfigurationError{
        if(System.getSecurityManager()!=null){
            //this will ensure bootclassloader is used
            return null;
        }
        // Figure out which ClassLoader to use for loading the provider
        // class.  If there is a Context ClassLoader then use it.
        ClassLoader context=SecuritySupport.getContextClassLoader();
        ClassLoader system=SecuritySupport.getSystemClassLoader();
        ClassLoader chain=system;
        while(true){
            if(context==chain){
                // Assert: we are on JDK 1.1 or we have no Context ClassLoader
                // or any Context ClassLoader in chain of system classloader
                // (including extension ClassLoader) so extend to widest
                // ClassLoader (always look in system ClassLoader if Xerces
                // is in boot/extension/system classpath and in current
                // ClassLoader otherwise); normal classloaders delegate
                // back to system ClassLoader first so this widening doesn't
                // change the fact that context ClassLoader will be consulted
                ClassLoader current=ObjectFactory.class.getClassLoader();
                chain=system;
                while(true){
                    if(current==chain){
                        // Assert: Current ClassLoader in chain of
                        // boot/extension/system ClassLoaders
                        return system;
                    }
                    if(chain==null){
                        break;
                    }
                    chain=SecuritySupport.getParentClassLoader(chain);
                }
                // Assert: Current ClassLoader not in chain of
                // boot/extension/system ClassLoaders
                return current;
            }
            if(chain==null){
                // boot ClassLoader reached
                break;
            }
            // Check for any extension ClassLoaders in chain up to
            // boot ClassLoader
            chain=SecuritySupport.getParentClassLoader(chain);
        }
        // Assert: Context ClassLoader not in chain of
        // boot/extension/system ClassLoaders
        return context;
    } // findClassLoader():ClassLoader

    public static Object newInstance(String className,ClassLoader cl,
                                     boolean doFallback)
            throws ConfigurationError{
        // assert(className != null);
        try{
            Class providerClass=findProviderClass(className,cl,doFallback);
            Object instance=providerClass.newInstance();
            if(DEBUG) debugPrintln("created new instance of "+providerClass+
                    " using ClassLoader: "+cl);
            return instance;
        }catch(ClassNotFoundException x){
            throw new ConfigurationError(
                    "Provider "+className+" not found",x);
        }catch(Exception x){
            throw new ConfigurationError(
                    "Provider "+className+" could not be instantiated: "+x,
                    x);
        }
    }

    private static void debugPrintln(String msg){
        if(DEBUG){
            System.err.println("XERCES: "+msg);
        }
    } // debugPrintln(String)

    public static Class findProviderClass(String className,ClassLoader cl,
                                          boolean doFallback)
            throws ClassNotFoundException, ConfigurationError{
        //throw security exception if the calling thread is not allowed to access the package
        //restrict the access to package as speicified in java.security policy
        SecurityManager security=System.getSecurityManager();
        if(security!=null){
            if(className.startsWith(JAXP_INTERNAL)||
                    className.startsWith(STAX_INTERNAL)){
                cl=null;
            }else{
                final int lastDot=className.lastIndexOf(".");
                String packageName=className;
                if(lastDot!=-1) packageName=className.substring(0,lastDot);
                security.checkPackageAccess(packageName);
            }
        }
        Class providerClass;
        if(cl==null){
            //use the bootstrap ClassLoader.
            providerClass=Class.forName(className,false,ObjectFactory.class.getClassLoader());
        }else{
            try{
                providerClass=cl.loadClass(className);
            }catch(ClassNotFoundException x){
                if(doFallback){
                    // Fall back to current classloader
                    ClassLoader current=ObjectFactory.class.getClassLoader();
                    if(current==null){
                        providerClass=Class.forName(className);
                    }else if(cl!=current){
                        cl=current;
                        providerClass=cl.loadClass(className);
                    }else{
                        throw x;
                    }
                }else{
                    throw x;
                }
            }
        }
        return providerClass;
    }

    public static Class findProviderClass(String className,boolean doFallback)
            throws ClassNotFoundException, ConfigurationError{
        return findProviderClass(className,
                findClassLoader(),doFallback);
    }
} // class ObjectFactory
