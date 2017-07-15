/**
 * Copyright (c) 2009, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.jgss;

import java.security.BasicPermission;

@jdk.Exported
public final class InquireSecContextPermission extends BasicPermission{
    private static final long serialVersionUID=-7131173349668647297L;

    public InquireSecContextPermission(String name){
        super(name);
    }
}
