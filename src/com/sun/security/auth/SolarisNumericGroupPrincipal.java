/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth;

import java.security.Principal;

@jdk.Exported(false)
@Deprecated
public class SolarisNumericGroupPrincipal implements
        Principal,
        java.io.Serializable{
    private static final long serialVersionUID=2345199581042573224L;
    private static final java.util.ResourceBundle rb=
            java.security.AccessController.doPrivileged
                    (new java.security.PrivilegedAction<java.util.ResourceBundle>(){
                        public java.util.ResourceBundle run(){
                            return (java.util.ResourceBundle.getBundle
                                    ("sun.security.util.AuthResources"));
                        }
                    });
    private String name;
    private boolean primaryGroup;

    public SolarisNumericGroupPrincipal(String name,boolean primaryGroup){
        if(name==null)
            throw new NullPointerException(rb.getString("provided.null.name"));
        this.name=name;
        this.primaryGroup=primaryGroup;
    }

    public SolarisNumericGroupPrincipal(long name,boolean primaryGroup){
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
        if(!(o instanceof SolarisNumericGroupPrincipal))
            return false;
        SolarisNumericGroupPrincipal that=(SolarisNumericGroupPrincipal)o;
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
        return ((primaryGroup?
                rb.getString
                        ("SolarisNumericGroupPrincipal.Primary.Group.")+name:
                rb.getString
                        ("SolarisNumericGroupPrincipal.Supplementary.Group.")+name));
    }
}
