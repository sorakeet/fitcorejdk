/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.jgss;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSName;

import javax.security.auth.Subject;

@jdk.Exported
public class GSSUtil{
    public static Subject createSubject(GSSName principals,
                                        GSSCredential credentials){
        return sun.security.jgss.GSSUtil.getSubject(principals,
                credentials);
    }
}
