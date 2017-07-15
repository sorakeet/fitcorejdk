/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth;

@jdk.Exported
public class NTNumericCredential{
    private long impersonationToken;

    public NTNumericCredential(long token){
        this.impersonationToken=token;
    }

    public long getToken(){
        return impersonationToken;
    }

    public int hashCode(){
        return (int)this.impersonationToken;
    }

    public boolean equals(Object o){
        if(o==null)
            return false;
        if(this==o)
            return true;
        if(!(o instanceof NTNumericCredential))
            return false;
        NTNumericCredential that=(NTNumericCredential)o;
        if(impersonationToken==that.getToken())
            return true;
        return false;
    }

    public String toString(){
        java.text.MessageFormat form=new java.text.MessageFormat
                (sun.security.util.ResourcesMgr.getString
                        ("NTNumericCredential.name",
                                "sun.security.util.AuthResources"));
        Object[] source={Long.toString(impersonationToken)};
        return form.format(source);
    }
}
