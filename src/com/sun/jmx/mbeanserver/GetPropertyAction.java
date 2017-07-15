/**
 * Copyright (c) 2002, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.mbeanserver;

import java.security.PrivilegedAction;

public class GetPropertyAction implements PrivilegedAction<String>{
    private final String key;

    public GetPropertyAction(String key){
        this.key=key;
    }

    public String run(){
        return System.getProperty(key);
    }
}
