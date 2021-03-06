/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002-2004 The Apache Software Foundation.
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
 * <p>
 * $Id: SecuritySupport.java,v 1.1.2.1 2005/08/01 02:08:48 jeffsuttor Exp $
 */
/**
 * Copyright 2002-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: SecuritySupport.java,v 1.1.2.1 2005/08/01 02:08:48 jeffsuttor Exp $
 */
package com.sun.org.apache.xalan.internal.utils;

import java.io.*;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;

public final class SecuritySupport{
    static final Properties cacheProps=new Properties();
    private static final SecuritySupport securitySupport=new SecuritySupport();
    static volatile boolean firstTime=true;

    private SecuritySupport(){
    }

    public static SecuritySupport getInstance(){
        return securitySupport;
    }

    public static ClassLoader getContextClassLoader(){
        return (ClassLoader)AccessController.doPrivileged(new PrivilegedAction(){
            public Object run(){
                ClassLoader cl=null;
                try{
                    cl=Thread.currentThread().getContextClassLoader();
                }catch(SecurityException ex){
                }
                return cl;
            }
        });
    }

    static ClassLoader getSystemClassLoader(){
        return (ClassLoader)AccessController.doPrivileged(new PrivilegedAction(){
            public Object run(){
                ClassLoader cl=null;
                try{
                    cl=ClassLoader.getSystemClassLoader();
                }catch(SecurityException ex){
                }
                return cl;
            }
        });
    }

    static ClassLoader getParentClassLoader(final ClassLoader cl){
        return (ClassLoader)AccessController.doPrivileged(new PrivilegedAction(){
            public Object run(){
                ClassLoader parent=null;
                try{
                    parent=cl.getParent();
                }catch(SecurityException ex){
                }
                // eliminate loops in case of the boot
                // ClassLoader returning itself as a parent
                return (parent==cl)?null:parent;
            }
        });
    }

    public static String getSystemProperty(final String propName,final String def){
        return (String)AccessController.doPrivileged(new PrivilegedAction(){
            public Object run(){
                return System.getProperty(propName,def);
            }
        });
    }

    public static InputStream getResourceAsStream(final String name){
        if(System.getSecurityManager()!=null){
            return getResourceAsStream(null,name);
        }else{
            return getResourceAsStream(ObjectFactory.findClassLoader(),name);
        }
    }

    public static InputStream getResourceAsStream(final ClassLoader cl,
                                                  final String name){
        return (InputStream)AccessController.doPrivileged(new PrivilegedAction(){
            public Object run(){
                InputStream ris;
                if(cl==null){
                    ris=Object.class.getResourceAsStream("/"+name);
                }else{
                    ris=cl.getResourceAsStream(name);
                }
                return ris;
            }
        });
    }

    public static ListResourceBundle getResourceBundle(String bundle){
        return getResourceBundle(bundle,Locale.getDefault());
    }

    public static ListResourceBundle getResourceBundle(final String bundle,final Locale locale){
        return AccessController.doPrivileged(new PrivilegedAction<ListResourceBundle>(){
            public ListResourceBundle run(){
                try{
                    return (ListResourceBundle)ResourceBundle.getBundle(bundle,locale);
                }catch(MissingResourceException e){
                    try{
                        return (ListResourceBundle)ResourceBundle.getBundle(bundle,new Locale("en","US"));
                    }catch(MissingResourceException e2){
                        throw new MissingResourceException(
                                "Could not load any resource bundle by "+bundle,bundle,"");
                    }
                }
            }
        });
    }

    static long getLastModified(final File f){
        return ((Long)AccessController.doPrivileged(new PrivilegedAction(){
            public Object run(){
                return new Long(f.lastModified());
            }
        })).longValue();
    }

    public static String sanitizePath(String uri){
        if(uri==null){
            return "";
        }
        int i=uri.lastIndexOf("/");
        if(i>0){
            return uri.substring(i+1,uri.length());
        }
        return "";
    }

    public static String checkAccess(String systemId,String allowedProtocols,String accessAny) throws IOException{
        if(systemId==null||(allowedProtocols!=null&&
                allowedProtocols.equalsIgnoreCase(accessAny))){
            return null;
        }
        String protocol;
        if(systemId.indexOf(":")==-1){
            protocol="file";
        }else{
            URL url=new URL(systemId);
            protocol=url.getProtocol();
            if(protocol.equalsIgnoreCase("jar")){
                String path=url.getPath();
                protocol=path.substring(0,path.indexOf(":"));
            }
        }
        if(isProtocolAllowed(protocol,allowedProtocols)){
            //access allowed
            return null;
        }else{
            return protocol;
        }
    }

    private static boolean isProtocolAllowed(String protocol,String allowedProtocols){
        if(allowedProtocols==null){
            return false;
        }
        String temp[]=allowedProtocols.split(",");
        for(String t : temp){
            t=t.trim();
            if(t.equalsIgnoreCase(protocol)){
                return true;
            }
        }
        return false;
    }

    public static String getJAXPSystemProperty(String sysPropertyId){
        String accessExternal=getSystemProperty(sysPropertyId);
        if(accessExternal==null){
            accessExternal=readJAXPProperty(sysPropertyId);
        }
        return accessExternal;
    }

    public static String getSystemProperty(final String propName){
        return (String)AccessController.doPrivileged(new PrivilegedAction(){
            public Object run(){
                return System.getProperty(propName);
            }
        });
    }

    static String readJAXPProperty(String propertyId){
        String value=null;
        InputStream is=null;
        try{
            if(firstTime){
                synchronized(cacheProps){
                    if(firstTime){
                        String configFile=getSystemProperty("java.home")+File.separator+
                                "lib"+File.separator+"jaxp.properties";
                        File f=new File(configFile);
                        if(getFileExists(f)){
                            is=getFileInputStream(f);
                            cacheProps.load(is);
                        }
                        firstTime=false;
                    }
                }
            }
            value=cacheProps.getProperty(propertyId);
        }catch(Exception ex){
        }finally{
            if(is!=null){
                try{
                    is.close();
                }catch(IOException ex){
                }
            }
        }
        return value;
    }

    static FileInputStream getFileInputStream(final File file)
            throws FileNotFoundException{
        try{
            return (FileInputStream)AccessController.doPrivileged(new PrivilegedExceptionAction(){
                public Object run() throws FileNotFoundException{
                    return new FileInputStream(file);
                }
            });
        }catch(PrivilegedActionException e){
            throw (FileNotFoundException)e.getException();
        }
    }

    public static boolean getFileExists(final File f){
        return ((Boolean)AccessController.doPrivileged(new PrivilegedAction(){
            public Object run(){
                return f.exists()?Boolean.TRUE:Boolean.FALSE;
            }
        })).booleanValue();
    }
}
