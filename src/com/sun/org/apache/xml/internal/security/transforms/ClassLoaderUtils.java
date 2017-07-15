/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.sun.org.apache.xml.internal.security.transforms;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

// NOTE! This is a duplicate of utils.ClassLoaderUtils with public
// modifiers changed to package-private. Make sure to integrate any future
// changes to utils.ClassLoaderUtils to this file.
final class ClassLoaderUtils{
    private static final java.util.logging.Logger log=
            java.util.logging.Logger.getLogger(ClassLoaderUtils.class.getName());

    private ClassLoaderUtils(){
    }

    static URL getResource(String resourceName,Class<?> callingClass){
        URL url=Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if(url==null&&resourceName.startsWith("/")){
            //certain classloaders need it without the leading /
            url=
                    Thread.currentThread().getContextClassLoader().getResource(
                            resourceName.substring(1)
                    );
        }
        ClassLoader cluClassloader=ClassLoaderUtils.class.getClassLoader();
        if(cluClassloader==null){
            cluClassloader=ClassLoader.getSystemClassLoader();
        }
        if(url==null){
            url=cluClassloader.getResource(resourceName);
        }
        if(url==null&&resourceName.startsWith("/")){
            //certain classloaders need it without the leading /
            url=cluClassloader.getResource(resourceName.substring(1));
        }
        if(url==null){
            ClassLoader cl=callingClass.getClassLoader();
            if(cl!=null){
                url=cl.getResource(resourceName);
            }
        }
        if(url==null){
            url=callingClass.getResource(resourceName);
        }
        if((url==null)&&(resourceName!=null)&&(resourceName.charAt(0)!='/')){
            return getResource('/'+resourceName,callingClass);
        }
        return url;
    }

    static List<URL> getResources(String resourceName,Class<?> callingClass){
        List<URL> ret=new ArrayList<URL>();
        Enumeration<URL> urls=new Enumeration<URL>(){
            public boolean hasMoreElements(){
                return false;
            }

            public URL nextElement(){
                return null;
            }
        };
        try{
            urls=Thread.currentThread().getContextClassLoader().getResources(resourceName);
        }catch(IOException e){
            if(log.isLoggable(java.util.logging.Level.FINE)){
                log.log(java.util.logging.Level.FINE,e.getMessage(),e);
            }
            //ignore
        }
        if(!urls.hasMoreElements()&&resourceName.startsWith("/")){
            //certain classloaders need it without the leading /
            try{
                urls=
                        Thread.currentThread().getContextClassLoader().getResources(
                                resourceName.substring(1)
                        );
            }catch(IOException e){
                if(log.isLoggable(java.util.logging.Level.FINE)){
                    log.log(java.util.logging.Level.FINE,e.getMessage(),e);
                }
                // ignore
            }
        }
        ClassLoader cluClassloader=ClassLoaderUtils.class.getClassLoader();
        if(cluClassloader==null){
            cluClassloader=ClassLoader.getSystemClassLoader();
        }
        if(!urls.hasMoreElements()){
            try{
                urls=cluClassloader.getResources(resourceName);
            }catch(IOException e){
                if(log.isLoggable(java.util.logging.Level.FINE)){
                    log.log(java.util.logging.Level.FINE,e.getMessage(),e);
                }
                // ignore
            }
        }
        if(!urls.hasMoreElements()&&resourceName.startsWith("/")){
            //certain classloaders need it without the leading /
            try{
                urls=cluClassloader.getResources(resourceName.substring(1));
            }catch(IOException e){
                if(log.isLoggable(java.util.logging.Level.FINE)){
                    log.log(java.util.logging.Level.FINE,e.getMessage(),e);
                }
                // ignore
            }
        }
        if(!urls.hasMoreElements()){
            ClassLoader cl=callingClass.getClassLoader();
            if(cl!=null){
                try{
                    urls=cl.getResources(resourceName);
                }catch(IOException e){
                    if(log.isLoggable(java.util.logging.Level.FINE)){
                        log.log(java.util.logging.Level.FINE,e.getMessage(),e);
                    }
                    // ignore
                }
            }
        }
        if(!urls.hasMoreElements()){
            URL url=callingClass.getResource(resourceName);
            if(url!=null){
                ret.add(url);
            }
        }
        while(urls.hasMoreElements()){
            ret.add(urls.nextElement());
        }
        if(ret.isEmpty()&&(resourceName!=null)&&(resourceName.charAt(0)!='/')){
            return getResources('/'+resourceName,callingClass);
        }
        return ret;
    }

    static InputStream getResourceAsStream(String resourceName,Class<?> callingClass){
        URL url=getResource(resourceName,callingClass);
        try{
            return (url!=null)?url.openStream():null;
        }catch(IOException e){
            if(log.isLoggable(java.util.logging.Level.FINE)){
                log.log(java.util.logging.Level.FINE,e.getMessage(),e);
            }
            return null;
        }
    }

    static Class<?> loadClass(String className,Class<?> callingClass)
            throws ClassNotFoundException{
        try{
            ClassLoader cl=Thread.currentThread().getContextClassLoader();
            if(cl!=null){
                return cl.loadClass(className);
            }
        }catch(ClassNotFoundException e){
            if(log.isLoggable(java.util.logging.Level.FINE)){
                log.log(java.util.logging.Level.FINE,e.getMessage(),e);
            }
            //ignore
        }
        return loadClass2(className,callingClass);
    }

    private static Class<?> loadClass2(String className,Class<?> callingClass)
            throws ClassNotFoundException{
        try{
            return Class.forName(className);
        }catch(ClassNotFoundException ex){
            try{
                if(ClassLoaderUtils.class.getClassLoader()!=null){
                    return ClassLoaderUtils.class.getClassLoader().loadClass(className);
                }
            }catch(ClassNotFoundException exc){
                if(callingClass!=null&&callingClass.getClassLoader()!=null){
                    return callingClass.getClassLoader().loadClass(className);
                }
            }
            if(log.isLoggable(java.util.logging.Level.FINE)){
                log.log(java.util.logging.Level.FINE,ex.getMessage(),ex);
            }
            throw ex;
        }
    }
}
