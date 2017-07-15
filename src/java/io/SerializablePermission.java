/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.io;

import java.security.BasicPermission;

public final class SerializablePermission extends BasicPermission{
    private static final long serialVersionUID=8537212141160296410L;
    private String actions;

    public SerializablePermission(String name){
        super(name);
    }

    public SerializablePermission(String name,String actions){
        super(name,actions);
    }
}
