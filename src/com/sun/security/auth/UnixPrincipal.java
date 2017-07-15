/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth;

import java.security.Principal;

@jdk.Exported
public class UnixPrincipal implements Principal, java.io.Serializable{
    private static final long serialVersionUID=-2951667807323493631L;
    private String name;

    public UnixPrincipal(String name){
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
        return name.hashCode();
    }

    public boolean equals(Object o){
        if(o==null)
            return false;
        if(this==o)
            return true;
        if(!(o instanceof UnixPrincipal))
            return false;
        UnixPrincipal that=(UnixPrincipal)o;
        if(this.getName().equals(that.getName()))
            return true;
        return false;
    }

    public String getName(){
        return name;
    }

    public String toString(){
        java.text.MessageFormat form=new java.text.MessageFormat
                (sun.security.util.ResourcesMgr.getString
                        ("UnixPrincipal.name",
                                "sun.security.util.AuthResources"));
        Object[] source={name};
        return form.format(source);
    }
}
