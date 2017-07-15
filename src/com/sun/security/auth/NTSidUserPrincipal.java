/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth;

@jdk.Exported
public class NTSidUserPrincipal extends NTSid{
    private static final long serialVersionUID=-5573239889517749525L;

    public NTSidUserPrincipal(String name){
        super(name);
    }

    public String toString(){
        java.text.MessageFormat form=new java.text.MessageFormat
                (sun.security.util.ResourcesMgr.getString
                        ("NTSidUserPrincipal.name",
                                "sun.security.util.AuthResources"));
        Object[] source={getName()};
        return form.format(source);
    }

    public boolean equals(Object o){
        if(o==null)
            return false;
        if(this==o)
            return true;
        if(!(o instanceof NTSidUserPrincipal))
            return false;
        return super.equals(o);
    }
}
