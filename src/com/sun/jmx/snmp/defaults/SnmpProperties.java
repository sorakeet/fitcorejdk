/**
 * Copyright (c) 2002, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.defaults;
// java import
//

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

public class SnmpProperties{
    // PUBLIC STATIC VARIABLES
    //------------------------
    public static final String MLET_LIB_DIR="jmx.mlet.library.dir";
    // PUBLIC STATIC METHODS
    //----------------------
    public static final String ACL_FILE="jdmk.acl.file";
    public static final String SECURITY_FILE="jdmk.security.file";
    public static final String UACL_FILE="jdmk.uacl.file";
    public static final String MIB_CORE_FILE="mibcore.file";
    public static final String JMX_SPEC_NAME="jmx.specification.name";
    public static final String JMX_SPEC_VERSION="jmx.specification.version";
    public static final String JMX_SPEC_VENDOR="jmx.specification.vendor";
    public static final String JMX_IMPL_NAME="jmx.implementation.name";
    public static final String JMX_IMPL_VENDOR="jmx.implementation.vendor";
    public static final String JMX_IMPL_VERSION="jmx.implementation.version";
    public static final String SSL_CIPHER_SUITE="jdmk.ssl.cipher.suite.";
    // private constructor defined to "hide" the default public constructor
    private SnmpProperties(){
    }

    public static void load(String file) throws IOException{
        Properties props=new Properties();
        InputStream is=new FileInputStream(file);
        props.load(is);
        is.close();
        for(final Enumeration<?> e=props.keys();e.hasMoreElements();){
            final String key=(String)e.nextElement();
            System.setProperty(key,props.getProperty(key));
        }
    }
}
