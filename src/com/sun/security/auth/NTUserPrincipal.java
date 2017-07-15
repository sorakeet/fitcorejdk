/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth;

import java.security.Principal;

@jdk.Exported
public class NTUserPrincipal implements Principal, java.io.Serializable{
    private static final long serialVersionUID=-8737649811939033735L;
    private String name;

    public NTUserPrincipal(String name){
        if(name==null){
            java.text.MessageFormat form=new java.text.MessageFormat
                    (sun.security.util.ResourcesMgr.getString
                            ("invalid.null.input.value",
                                    "sun.security.util.AuthResources"));
            Object[] source={"name"};
            throw new NullPointerException(form.format(source));
        }
        this.name=name;
    }

    public int hashCode(){
        return this.getName().hashCode();
    }

    public String getName(){
        return name;
    }

    public boolean equals(Object o){
        if(o==null)
            return false;
        if(this==o)
            return true;
        if(!(o instanceof NTUserPrincipal))
            return false;
        NTUserPrincipal that=(NTUserPrincipal)o;
        if(name.equals(that.getName()))
            return true;
        return false;
    }

    public String toString(){
        java.text.MessageFormat form=new java.text.MessageFormat
                (sun.security.util.ResourcesMgr.getString
                        ("NTUserPrincipal.name",
                                "sun.security.util.AuthResources"));
        Object[] source={name};
        return form.format(source);
    }
}
