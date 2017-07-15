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
import java.util.StringTokenizer;
import java.util.Vector;

public abstract class VersionHelper{
    public final static int INITIAL_CONTEXT_FACTORY=0;
    public final static int OBJECT_FACTORIES=1;
    public final static int URL_PKG_PREFIXES=2;
    public final static int STATE_FACTORIES=3;
    public final static int PROVIDER_URL=4;
    public final static int DNS_URL=5;
    public final static int CONTROL_FACTORIES=6;
    final static String[] PROPS=new String[]{
            javax.naming.Context.INITIAL_CONTEXT_FACTORY,
            javax.naming.Context.OBJECT_FACTORIES,
            javax.naming.Context.URL_PKG_PREFIXES,
            javax.naming.Context.STATE_FACTORIES,
            javax.naming.Context.PROVIDER_URL,
            javax.naming.Context.DNS_URL,
            // The following shouldn't create a runtime dependence on ldap package.
            javax.naming.ldap.LdapContext.CONTROL_FACTORIES
    };
    private static VersionHelper helper=null;

    static{
        helper=new VersionHelper12();
    }

    VersionHelper(){
    } // Disallow anyone from creating one of these.

    public static VersionHelper getVersionHelper(){
        return helper;
    }

    static protected URL[] getUrlArray(String codebase)
            throws MalformedURLException{
        // Parse codebase into separate URLs
        StringTokenizer parser=new StringTokenizer(codebase);
        Vector<String> vec=new Vector<>(10);
        while(parser.hasMoreTokens()){
            vec.addElement(parser.nextToken());
        }
        String[] url=new String[vec.size()];
        for(int i=0;i<url.length;i++){
            url[i]=vec.elementAt(i);
        }
        URL[] urlArray=new URL[url.length];
        for(int i=0;i<urlArray.length;i++){
            urlArray[i]=new URL(url[i]);
        }
        return urlArray;
    }

    public abstract Class<?> loadClass(String className)
            throws ClassNotFoundException;

    abstract Class<?> loadClass(String className,ClassLoader cl)
            throws ClassNotFoundException;

    public abstract Class<?> loadClass(String className,String codebase)
            throws ClassNotFoundException, MalformedURLException;

    abstract String getJndiProperty(int i);

    abstract String[] getJndiProperties();

    abstract InputStream getResourceAsStream(Class<?> c,String name);

    abstract InputStream getJavaHomeLibStream(String filename);

    abstract NamingEnumeration<InputStream> getResources(
            ClassLoader cl,String name)
            throws IOException;

    abstract ClassLoader getContextClassLoader();
}
