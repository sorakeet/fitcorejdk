/**
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2004 World Wide Web Consortium,
 * <p>
 * (Massachusetts Institute of Technology, European Research Consortium for
 * Informatics and Mathematics, Keio University). All Rights Reserved. This
 * work is distributed under the W3C(r) Software License [1] in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */
/**
 *
 *
 *
 *
 *
 * Copyright (c) 2004 World Wide Web Consortium,
 *
 * (Massachusetts Institute of Technology, European Research Consortium for
 * Informatics and Mathematics, Keio University). All Rights Reserved. This
 * work is distributed under the W3C(r) Software License [1] in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */
package org.w3c.dom.bootstrap;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMImplementationList;
import org.w3c.dom.DOMImplementationSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.StringTokenizer;
import java.util.Vector;

public final class DOMImplementationRegistry{
    public static final String PROPERTY=
            "org.w3c.dom.DOMImplementationSourceList";
    private static final int DEFAULT_LINE_LENGTH=80;
    private static final String FALLBACK_CLASS=
            "com.sun.org.apache.xerces.internal.dom.DOMXSImplementationSourceImpl";
    private static final String DEFAULT_PACKAGE=
            "com.sun.org.apache.xerces.internal.dom";
    private Vector sources;

    private DOMImplementationRegistry(final Vector srcs){
        sources=srcs;
    }

    public static DOMImplementationRegistry newInstance()
            throws
            ClassNotFoundException,
            InstantiationException,
            IllegalAccessException,
            ClassCastException{
        Vector sources=new Vector();
        ClassLoader classLoader=getClassLoader();
        // fetch system property:
        String p=getSystemProperty(PROPERTY);
        //
        // if property is not specified then use contents of
        // META_INF/org.w3c.dom.DOMImplementationSourceList from classpath
        if(p==null){
            p=getServiceValue(classLoader);
        }
        if(p==null){
            //
            // DOM Implementations can modify here to add *additional* fallback
            // mechanisms to access a list of default DOMImplementationSources.
            //fall back to JAXP implementation class com.sun.org.apache.xerces.internal.dom.DOMXSImplementationSourceImpl
            p=FALLBACK_CLASS;
        }
        if(p!=null){
            StringTokenizer st=new StringTokenizer(p);
            while(st.hasMoreTokens()){
                String sourceName=st.nextToken();
                // make sure we have access to restricted packages
                boolean internal=false;
                if(System.getSecurityManager()!=null){
                    if(sourceName!=null&&sourceName.startsWith(DEFAULT_PACKAGE)){
                        internal=true;
                    }
                }
                Class sourceClass=null;
                if(classLoader!=null&&!internal){
                    sourceClass=classLoader.loadClass(sourceName);
                }else{
                    sourceClass=Class.forName(sourceName);
                }
                DOMImplementationSource source=
                        (DOMImplementationSource)sourceClass.newInstance();
                sources.addElement(source);
            }
        }
        return new DOMImplementationRegistry(sources);
    }

    private static ClassLoader getClassLoader(){
        try{
            ClassLoader contextClassLoader=getContextClassLoader();
            if(contextClassLoader!=null){
                return contextClassLoader;
            }
        }catch(Exception e){
            // Assume that the DOM application is in a JRE 1.1, use the
            // current ClassLoader
            return DOMImplementationRegistry.class.getClassLoader();
        }
        return DOMImplementationRegistry.class.getClassLoader();
    }

    private static ClassLoader getContextClassLoader(){
        return isJRE11()
                ?null
                :(ClassLoader)
                AccessController.doPrivileged(new PrivilegedAction(){
                    public Object run(){
                        ClassLoader classLoader=null;
                        try{
                            classLoader=
                                    Thread.currentThread().getContextClassLoader();
                        }catch(SecurityException ex){
                        }
                        return classLoader;
                    }
                });
    }

    private static boolean isJRE11(){
        try{
            Class c=Class.forName("java.security.AccessController");
            // java.security.AccessController existed since 1.2 so, if no
            // exception was thrown, the DOM application is running in a JRE
            // 1.2 or higher
            return false;
        }catch(Exception ex){
            // ignore
        }
        return true;
    }

    private static String getServiceValue(final ClassLoader classLoader){
        String serviceId="META-INF/services/"+PROPERTY;
        // try to find services in CLASSPATH
        try{
            InputStream is=getResourceAsStream(classLoader,serviceId);
            if(is!=null){
                BufferedReader rd;
                try{
                    rd=
                            new BufferedReader(new InputStreamReader(is,"UTF-8"),
                                    DEFAULT_LINE_LENGTH);
                }catch(java.io.UnsupportedEncodingException e){
                    rd=
                            new BufferedReader(new InputStreamReader(is),
                                    DEFAULT_LINE_LENGTH);
                }
                String serviceValue=rd.readLine();
                rd.close();
                if(serviceValue!=null&&serviceValue.length()>0){
                    return serviceValue;
                }
            }
        }catch(Exception ex){
            return null;
        }
        return null;
    }

    private static InputStream getResourceAsStream(final ClassLoader classLoader,
                                                   final String name){
        if(isJRE11()){
            InputStream ris;
            if(classLoader==null){
                ris=ClassLoader.getSystemResourceAsStream(name);
            }else{
                ris=classLoader.getResourceAsStream(name);
            }
            return ris;
        }else{
            return (InputStream)
                    AccessController.doPrivileged(new PrivilegedAction(){
                        public Object run(){
                            InputStream ris;
                            if(classLoader==null){
                                ris=
                                        ClassLoader.getSystemResourceAsStream(name);
                            }else{
                                ris=classLoader.getResourceAsStream(name);
                            }
                            return ris;
                        }
                    });
        }
    }

    private static String getSystemProperty(final String name){
        return isJRE11()
                ?(String)System.getProperty(name)
                :(String)AccessController.doPrivileged(new PrivilegedAction(){
            public Object run(){
                return System.getProperty(name);
            }
        });
    }

    public DOMImplementation getDOMImplementation(final String features){
        int size=sources.size();
        String name=null;
        for(int i=0;i<size;i++){
            DOMImplementationSource source=
                    (DOMImplementationSource)sources.elementAt(i);
            DOMImplementation impl=source.getDOMImplementation(features);
            if(impl!=null){
                return impl;
            }
        }
        return null;
    }

    public DOMImplementationList getDOMImplementationList(final String features){
        final Vector implementations=new Vector();
        int size=sources.size();
        for(int i=0;i<size;i++){
            DOMImplementationSource source=
                    (DOMImplementationSource)sources.elementAt(i);
            DOMImplementationList impls=
                    source.getDOMImplementationList(features);
            for(int j=0;j<impls.getLength();j++){
                DOMImplementation impl=impls.item(j);
                implementations.addElement(impl);
            }
        }
        return new DOMImplementationList(){
            public DOMImplementation item(final int index){
                if(index>=0&&index<implementations.size()){
                    try{
                        return (DOMImplementation)
                                implementations.elementAt(index);
                    }catch(ArrayIndexOutOfBoundsException e){
                        return null;
                    }
                }
                return null;
            }

            public int getLength(){
                return implementations.size();
            }
        };
    }

    public void addSource(final DOMImplementationSource s){
        if(s==null){
            throw new NullPointerException();
        }
        if(!sources.contains(s)){
            sources.addElement(s);
        }
    }
}
