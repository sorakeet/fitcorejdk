/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth;

public final class AuthPermission extends
        java.security.BasicPermission{
    private static final long serialVersionUID=5806031445061587174L;

    public AuthPermission(String name){
        // for backwards compatibility --
        // createLoginContext is deprecated in favor of createLoginContext.*
        super("createLoginContext".equals(name)?
                "createLoginContext.*":name);
    }

    public AuthPermission(String name,String actions){
        // for backwards compatibility --
        // createLoginContext is deprecated in favor of createLoginContext.*
        super("createLoginContext".equals(name)?
                "createLoginContext.*":name,actions);
    }
}
