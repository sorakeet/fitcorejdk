/**
 * Copyright (c) 2009, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.jgss;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;

@jdk.Exported
public interface ExtendedGSSContext extends GSSContext{
    public Object inquireSecContext(InquireType type)
            throws GSSException;

    public void requestDelegPolicy(boolean state) throws GSSException;

    public boolean getDelegPolicyState();
}
