/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth;

@jdk.Exported
public interface PrincipalComparator{
    boolean implies(javax.security.auth.Subject subject);
}
