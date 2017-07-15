/**
 * Copyright (c) 1999, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.naming.internal;

import javax.naming.NamingEnumeration;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Properties;

final class VersionHelper12 extends VersionHelper{
    // Disallow external from creating one of these.
    VersionHelper12(){
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException{
        return loadClass(className,getContextClassLoader());
    }

    Class<?> loadClass(String className,ClassLoader cl)
            throws ClassNotFoundException{
        Class<?> cls=Class.forName(className,true,cl);
        return cls;
    }

    public Class<?> loadClass(String className,String codebase)
            throws ClassNotFoundException, MalformedURLException{
        ClassLoader parent=getContextClassLoader();
        ClassLoader cl=
                URLClassLoader.newInstance(getUrlArray(codebase),parent);
        return loadClass(className,cl);
    }

    String getJndiProperty(final int i){
        return AccessController.doPrivileged(
                new PrivilegedAction<String>(){
                    public String run(){
                        try{
                            return System.getProperty(PROPS[i]);
                        }catch(SecurityException e){
                            return null;
                        }
                    }
                }
        );
    }

    String[] getJndiProperties(){
        Properties sysProps=AccessController.doPrivileged(
                new PrivilegedAction<Properties>(){
                    public Properties run(){
                        try{
                            return System.getProperties();
                        }catch(SecurityException e){
                            return null;
                        }
                    }
                }
        );
        if(sysProps==null){
            return null;
        }
        String[] jProps=new String[PROPS.length];
        for(int i=0;i<PROPS.length;i++){
            jProps[i]=sysProps.getProperty(PROPS[i]);
        }
        return jProps;
    }

    InputStream getResourceAsStream(final Class<?> c,final String name){
        return AccessController.doPrivileged(
                new PrivilegedAction<InputStream>(){
                    public InputStream run(){
                        return c.getResourceAsStream(name);
                    }
                }
        );
    }

    InputStream getJavaHomeLibStream(final String filename){
        return AccessController.doPrivileged(
                new PrivilegedAction<InputStream>(){
                    public InputStream run(){
                        try{
                            String javahome=System.getProperty("java.home");
                            if(javahome==null){
                                return null;
                            }
                            String pathname=javahome+java.io.File.separator+
                                    "lib"+java.io.File.separator+filename;
                            return new java.io.FileInputStream(pathname);
                        }catch(Exception e){
                            return null;
                        }
                    }
                }
        );
    }

    NamingEnumeration<InputStream> getResources(final ClassLoader cl,
                                                final String name) throws IOException{
        Enumeration<URL> urls;
        try{
            urls=AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Enumeration<URL>>(){
                        public Enumeration<URL> run() throws IOException{
                            return (cl==null)
                                    ?ClassLoader.getSystemResources(name)
                                    :cl.getResources(name);
                        }
                    }
            );
        }catch(PrivilegedActionException e){
            throw (IOException)e.getException();
        }
        return new InputStreamEnumeration(urls);
    }

    ClassLoader getContextClassLoader(){
        return AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>(){
                    public ClassLoader run(){
                        ClassLoader loader=
                                Thread.currentThread().getContextClassLoader();
                        if(loader==null){
                            // Don't use bootstrap class loader directly!
                            loader=ClassLoader.getSystemClassLoader();
                        }
                        return loader;
                    }
                }
        );
    }

    class InputStreamEnumeration implements NamingEnumeration<InputStream>{
        private final Enumeration<URL> urls;
        private InputStream nextElement=null;

        InputStreamEnumeration(Enumeration<URL> urls){
            this.urls=urls;
        }

        public boolean hasMoreElements(){
            return hasMore();
        }

        public InputStream nextElement(){
            return next();
        }

        public InputStream next(){
            if(hasMore()){
                InputStream res=nextElement;
                nextElement=null;
                return res;
            }else{
                throw new NoSuchElementException();
            }
        }

        public boolean hasMore(){
            if(nextElement!=null){
                return true;
            }
            nextElement=getNextElement();
            return (nextElement!=null);
        }

        private InputStream getNextElement(){
            return AccessController.doPrivileged(
                    new PrivilegedAction<InputStream>(){
                        public InputStream run(){
                            while(urls.hasMoreElements()){
                                try{
                                    return urls.nextElement().openStream();
                                }catch(IOException e){
                                    // skip this URL
                                }
                            }
                            return null;
                        }
                    }
            );
        }

        public void close(){
        }
    }
}
