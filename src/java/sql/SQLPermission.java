/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

import java.security.BasicPermission;

public final class SQLPermission extends BasicPermission{
    static final long serialVersionUID=-1439323187199563495L;

    public SQLPermission(String name){
        super(name);
    }

    public SQLPermission(String name,String actions){
        super(name,actions);
    }
}
