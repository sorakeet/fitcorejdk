/**
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.logging;

public abstract class CORBALogDomains{
    // Top level log domain for CORBA
    public static final String TOP_LEVEL_DOMAIN="javax.enterprise.resource.corba";
    public static final String RPC="rpc";
    public static final String RPC_PRESENTATION="rpc.presentation";
    public static final String RPC_ENCODING="rpc.encoding";
    public static final String RPC_PROTOCOL="rpc.protocol";
    public static final String RPC_TRANSPORT="rpc.transport";
    public static final String NAMING="naming";
    public static final String NAMING_LIFECYCLE="naming.lifecycle";
    public static final String NAMING_READ="naming.read";
    public static final String NAMING_UPDATE="naming.update";
    public static final String ORBD="orbd";
    public static final String ORBD_LOCATOR="orbd.locator";
    public static final String ORBD_ACTIVATOR="orbd.activator";
    public static final String ORBD_REPOSITORY="orbd.repository";
    public static final String ORBD_SERVERTOOL="orbd.servertool";
    public static final String ORB="orb";
    public static final String ORB_LIFECYCLE="orb.lifecycle";
    public static final String ORB_RESOLVER="orb.resolver";
    public static final String OA="oa";
    public static final String OA_LIFECYCLE="oa.lifecycle";
    public static final String OA_IOR="oa.ior";
    public static final String OA_INVOCATION="oa.invocation";
    public static final String RMIIIOP="rmiiiop";
    public static final String RMIIIOP_DELEGATE="rmiiiop.delegate";
    public static final String UTIL="util";
    private CORBALogDomains(){
    }
}
