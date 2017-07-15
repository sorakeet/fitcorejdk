/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * $Id: OutputPropertiesFactory.java,v 1.2.4.1 2005/09/15 08:15:21 suresh_emailid Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * $Id: OutputPropertiesFactory.java,v 1.2.4.1 2005/09/15 08:15:21 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import com.sun.org.apache.xml.internal.serializer.utils.MsgKey;
import com.sun.org.apache.xml.internal.serializer.utils.Utils;
import com.sun.org.apache.xml.internal.serializer.utils.WrappedRuntimeException;

import javax.xml.transform.OutputKeys;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Properties;

public final class OutputPropertiesFactory{
    // Some special Xalan keys.
    public static final String S_KEY_INDENT_AMOUNT=
            S_BUILTIN_EXTENSIONS_UNIVERSAL+"indent-amount";
    public static final String S_KEY_LINE_SEPARATOR=
            S_BUILTIN_EXTENSIONS_UNIVERSAL+"line-separator";
    public static final String S_KEY_CONTENT_HANDLER=
            S_BUILTIN_EXTENSIONS_UNIVERSAL+"content-handler";
    public static final String S_KEY_ENTITIES=
            S_BUILTIN_EXTENSIONS_UNIVERSAL+"entities";
    public static final String S_USE_URL_ESCAPING=
            S_BUILTIN_EXTENSIONS_UNIVERSAL+"use-url-escaping";
    public static final String S_OMIT_META_TAG=
            S_BUILTIN_EXTENSIONS_UNIVERSAL+"omit-meta-tag";
    public static final int S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL_LEN=
            S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL.length();
    public static final String ORACLE_IS_STANDALONE="http://www.oracle.com/xml/is-standalone";
    private static final String
            S_BUILTIN_EXTENSIONS_URL="http://xml.apache.org/xalan";
    //************************************************************
    //*  PUBLIC CONSTANTS
    //************************************************************
    public static final String S_BUILTIN_EXTENSIONS_UNIVERSAL=
            "{"+S_BUILTIN_EXTENSIONS_URL+"}";
    private static final String
            S_BUILTIN_OLD_EXTENSIONS_URL="http://xml.apache.org/xslt";
    public static final String S_BUILTIN_OLD_EXTENSIONS_UNIVERSAL=
            "{"+S_BUILTIN_OLD_EXTENSIONS_URL+"}";
    //************************************************************
    //*  PRIVATE CONSTANTS
    //************************************************************
    private static final String S_XSLT_PREFIX="xslt.output.";
    private static final int S_XSLT_PREFIX_LEN=S_XSLT_PREFIX.length();
    private static final String S_XALAN_PREFIX="org.apache.xslt.";
    private static final int S_XALAN_PREFIX_LEN=S_XALAN_PREFIX.length();
    private static final String PROP_DIR="com/sun/org/apache/xml/internal/serializer/";
    private static final String PROP_FILE_XML="output_xml.properties";
    private static final String PROP_FILE_TEXT="output_text.properties";
    private static final String PROP_FILE_HTML="output_html.properties";
    private static final String PROP_FILE_UNKNOWN="output_unknown.properties";
    private static final Class
            ACCESS_CONTROLLER_CLASS=findAccessControllerClass();
    private static Integer m_synch_object=new Integer(1);
    //************************************************************
    //*  PRIVATE STATIC FIELDS
    //************************************************************
    private static Properties m_xml_properties=null;
    private static Properties m_html_properties=null;
    private static Properties m_text_properties=null;
    private static Properties m_unknown_properties=null;

    private static Class findAccessControllerClass(){
        try{
            // This Class was introduced in JDK 1.2. With the re-architecture of
            // security mechanism ( starting in JDK 1.2 ), we have option of
            // giving privileges to certain part of code using doPrivileged block.
            // In JDK1.1.X applications won't be having security manager and if
            // there is security manager ( in applets ), code need to be signed
            // and trusted for having access to resources.
            return Class.forName("java.security.AccessController");
        }catch(Exception e){
            //User may be using older JDK ( JDK <1.2 ). Allow him/her to use it.
            // But don't try to use doPrivileged
        }
        return null;
    }

    static public final Properties getDefaultMethodProperties(String method){
        String fileName=null;
        Properties defaultProperties=null;
        // According to this article : Double-check locking does not work
        // http://www.javaworld.com/javaworld/jw-02-2001/jw-0209-toolbox.html
        try{
            synchronized(m_synch_object){
                if(null==m_xml_properties) // double check
                {
                    fileName=PROP_FILE_XML;
                    m_xml_properties=loadPropertiesFile(fileName,null);
                }
            }
            if(method.equals(Method.XML)){
                defaultProperties=m_xml_properties;
            }else if(method.equals(Method.HTML)){
                if(null==m_html_properties) // double check
                {
                    fileName=PROP_FILE_HTML;
                    m_html_properties=
                            loadPropertiesFile(fileName,m_xml_properties);
                }
                defaultProperties=m_html_properties;
            }else if(method.equals(Method.TEXT)){
                if(null==m_text_properties) // double check
                {
                    fileName=PROP_FILE_TEXT;
                    m_text_properties=
                            loadPropertiesFile(fileName,m_xml_properties);
                    if(null
                            ==m_text_properties.getProperty(OutputKeys.ENCODING)){
                        String mimeEncoding=Encodings.getMimeEncoding(null);
                        m_text_properties.put(
                                OutputKeys.ENCODING,
                                mimeEncoding);
                    }
                }
                defaultProperties=m_text_properties;
            }else if(method.equals(Method.UNKNOWN)){
                if(null==m_unknown_properties) // double check
                {
                    fileName=PROP_FILE_UNKNOWN;
                    m_unknown_properties=
                            loadPropertiesFile(fileName,m_xml_properties);
                }
                defaultProperties=m_unknown_properties;
            }else{
                // TODO: Calculate res file from name.
                defaultProperties=m_xml_properties;
            }
        }catch(IOException ioe){
            throw new WrappedRuntimeException(
                    Utils.messages.createMessage(
                            MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY,
                            new Object[]{fileName,method}),
                    ioe);
        }
        // wrap these cached defaultProperties in a new Property object just so
        // that the caller of this method can't modify the default values
        return new Properties(defaultProperties);
    }

    static private Properties loadPropertiesFile(
            final String resourceName,
            Properties defaults)
            throws IOException{
        // This static method should eventually be moved to a thread-specific class
        // so that we can cache the ContextClassLoader and bottleneck all properties file
        // loading throughout Xalan.
        Properties props=new Properties(defaults);
        InputStream is=null;
        BufferedInputStream bis=null;
        try{
            if(ACCESS_CONTROLLER_CLASS!=null){
                is=(InputStream)AccessController
                        .doPrivileged(new PrivilegedAction(){
                            public Object run(){
                                return OutputPropertiesFactory.class
                                        .getResourceAsStream(resourceName);
                            }
                        });
            }else{
                // User may be using older JDK ( JDK < 1.2 )
                is=OutputPropertiesFactory.class
                        .getResourceAsStream(resourceName);
            }
            bis=new BufferedInputStream(is);
            props.load(bis);
        }catch(IOException ioe){
            if(defaults==null){
                throw ioe;
            }else{
                throw new WrappedRuntimeException(
                        Utils.messages.createMessage(
                                MsgKey.ER_COULD_NOT_LOAD_RESOURCE,
                                new Object[]{resourceName}),
                        ioe);
                //"Could not load '"+resourceName+"' (check CLASSPATH), now using just the defaults ", ioe);
            }
        }catch(SecurityException se){
            // Repeat IOException handling for sandbox/applet case -sc
            if(defaults==null){
                throw se;
            }else{
                throw new WrappedRuntimeException(
                        Utils.messages.createMessage(
                                MsgKey.ER_COULD_NOT_LOAD_RESOURCE,
                                new Object[]{resourceName}),
                        se);
                //"Could not load '"+resourceName+"' (check CLASSPATH, applet security), now using just the defaults ", se);
            }
        }finally{
            if(bis!=null){
                bis.close();
            }
            if(is!=null){
                is.close();
            }
        }
        // Note that we're working at the HashTable level here,
        // and not at the Properties level!  This is important
        // because we don't want to modify the default properties.
        // NB: If fixupPropertyString ends up changing the property
        // name or value, we need to remove the old key and re-add
        // with the new key and value.  However, then our Enumeration
        // could lose its place in the HashTable.  So, we first
        // clone the HashTable and enumerate over that since the
        // clone will not change.  When we migrate to Collections,
        // this code should be revisited and cleaned up to use
        // an Iterator which may (or may not) alleviate the need for
        // the clone.  Many thanks to Padraig O'hIceadha
        // <padraig@gradient.ie> for finding this problem.  Bugzilla 2000.
        Enumeration keys=((Properties)props.clone()).keys();
        while(keys.hasMoreElements()){
            String key=(String)keys.nextElement();
            // Now check if the given key was specified as a
            // System property. If so, the system property
            // overides the default value in the propery file.
            String value=null;
            try{
                value=SecuritySupport.getSystemProperty(key);
            }catch(SecurityException se){
                // No-op for sandbox/applet case, leave null -sc
            }
            if(value==null)
                value=(String)props.get(key);
            String newKey=fixupPropertyString(key,true);
            String newValue=null;
            try{
                newValue=SecuritySupport.getSystemProperty(newKey);
            }catch(SecurityException se){
                // No-op for sandbox/applet case, leave null -sc
            }
            if(newValue==null)
                newValue=fixupPropertyString(value,false);
            else
                newValue=fixupPropertyString(newValue,false);
            if(key!=newKey||value!=newValue){
                props.remove(key);
                props.put(newKey,newValue);
            }
        }
        return props;
    }

    static private String fixupPropertyString(String s,boolean doClipping){
        int index;
        if(doClipping&&s.startsWith(S_XSLT_PREFIX)){
            s=s.substring(S_XSLT_PREFIX_LEN);
        }
        if(s.startsWith(S_XALAN_PREFIX)){
            s=
                    S_BUILTIN_EXTENSIONS_UNIVERSAL
                            +s.substring(S_XALAN_PREFIX_LEN);
        }
        if((index=s.indexOf("\\u003a"))>0){
            String temp=s.substring(index+6);
            s=s.substring(0,index)+":"+temp;
        }
        return s;
    }
}
