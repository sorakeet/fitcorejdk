/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth;

import javax.security.auth.Subject;
import java.security.CodeSource;
import java.security.PermissionCollection;

@jdk.Exported(false)
@Deprecated
public class PolicyFile extends javax.security.auth.Policy{
    private final sun.security.provider.AuthPolicyFile apf;

    public PolicyFile(){
        apf=new sun.security.provider.AuthPolicyFile();
    }

    @Override
    public PermissionCollection getPermissions(final Subject subject,
                                               final CodeSource codesource){
        return apf.getPermissions(subject,codesource);
    }

    @Override
    public void refresh(){
        apf.refresh();
    }
}
