/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.jgss;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;

@jdk.Exported
public interface ExtendedGSSCredential extends GSSCredential{
    public GSSCredential impersonate(GSSName name) throws GSSException;
}
