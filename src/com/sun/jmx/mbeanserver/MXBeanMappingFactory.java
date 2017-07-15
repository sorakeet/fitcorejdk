/**
 * Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.mbeanserver;

import javax.management.openmbean.OpenDataException;
import java.lang.reflect.Type;

public abstract class MXBeanMappingFactory{
    public static final MXBeanMappingFactory DEFAULT=
            new DefaultMXBeanMappingFactory();

    protected MXBeanMappingFactory(){
    }

    public abstract MXBeanMapping mappingForType(Type t,MXBeanMappingFactory f)
            throws OpenDataException;
}
