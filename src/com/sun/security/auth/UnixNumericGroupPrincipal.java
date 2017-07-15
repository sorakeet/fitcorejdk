/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth;

import java.security.Principal;

@jdk.Exported
public class UnixNumericGroupPrincipal implements
        Principal,
        java.io.Serializable{
    private static final long serialVersionUID=3941535899328403223L;
    private String name;
    private boolean primaryGroup;

    public UnixNumericGroupPrincipal(String name,boolean primaryGroup){
        if(name==null){
            java.text.MessageFormat form=new java.text.MessageFormat
                    (sun.security.util.ResourcesMgr.getString
                            ("invalid.null.input.value",
                                    "sun.security.util.AuthResources"));
            Object[] source={"name"};
            throw new NullPointerException(form.format(source));
        }
        this.name=name;
        this.primaryGroup=primaryGroup;
    }

    public UnixNumericGroupPrincipal(long name,boolean primaryGroup){
        this.name=(new Long(name)).toString();
        this.primaryGroup=primaryGroup;
    }

    public long longValue(){
        return ((new Long(name)).longValue());
    }

    public int hashCode(){
        return toString().hashCode();
    }

    public boolean equals(Object o){
        if(o==null)
            return false;
        if(this==o)
            return true;
        if(!(o instanceof UnixNumericGroupPrincipal))
            return false;
        UnixNumericGroupPrincipal that=(UnixNumericGroupPrincipal)o;
        if(this.getName().equals(that.getName())&&
                this.isPrimaryGroup()==that.isPrimaryGroup())
            return true;
        return false;
    }

    public String getName(){
        return name;
    }

    public boolean isPrimaryGroup(){
        return primaryGroup;
    }

    public String toString(){
        if(primaryGroup){
            java.text.MessageFormat form=new java.text.MessageFormat
                    (sun.security.util.ResourcesMgr.getString
                            ("UnixNumericGroupPrincipal.Primary.Group.name",
                                    "sun.security.util.AuthResources"));
            Object[] source={name};
            return form.format(source);
        }else{
            java.text.MessageFormat form=new java.text.MessageFormat
                    (sun.security.util.ResourcesMgr.getString
                            ("UnixNumericGroupPrincipal.Supplementary.Group.name",
                                    "sun.security.util.AuthResources"));
            Object[] source={name};
            return form.format(source);
        }
    }
}
