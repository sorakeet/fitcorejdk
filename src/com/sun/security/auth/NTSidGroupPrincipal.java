/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth;

@jdk.Exported
public class NTSidGroupPrincipal extends NTSid{
    private static final long serialVersionUID=-1373347438636198229L;

    public NTSidGroupPrincipal(String name){
        super(name);
    }

    public String toString(){
        java.text.MessageFormat form=new java.text.MessageFormat
                (sun.security.util.ResourcesMgr.getString
                        ("NTSidGroupPrincipal.name",
                                "sun.security.util.AuthResources"));
        Object[] source={getName()};
        return form.format(source);
    }

    public boolean equals(Object o){
        if(o==null)
            return false;
        if(this==o)
            return true;
        if(!(o instanceof NTSidGroupPrincipal))
            return false;
        return super.equals(o);
    }
}
