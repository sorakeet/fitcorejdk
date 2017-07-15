/**
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.defaults;

public class ServiceName{
    public static final String DELEGATE=
            "JMImplementation:type=MBeanServerDelegate";
    public static final String MLET="type=MLet";
    public static final String DOMAIN="DefaultDomain";
    public static final String JMX_SPEC_NAME="Java Management Extensions";
    public static final String JMX_SPEC_VERSION="1.4";
    public static final String JMX_SPEC_VENDOR="Oracle Corporation";
    public static final String JMX_IMPL_NAME="JMX";
    public static final String JMX_IMPL_VENDOR="Oracle Corporation";
    // private constructor defined to "hide" the default public constructor
    private ServiceName(){
    }
}
