/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth;

import java.security.Principal;

@jdk.Exported
public class NTSid implements Principal, java.io.Serializable{
    private static final long serialVersionUID=4412290580770249885L;
    private String sid;

    public NTSid(String stringSid){
        if(stringSid==null){
            java.text.MessageFormat form=new java.text.MessageFormat
                    (sun.security.util.ResourcesMgr.getString
                            ("invalid.null.input.value",
                                    "sun.security.util.AuthResources"));
            Object[] source={"stringSid"};
            throw new NullPointerException(form.format(source));
        }
        if(stringSid.length()==0){
            throw new IllegalArgumentException
                    (sun.security.util.ResourcesMgr.getString
                            ("Invalid.NTSid.value",
                                    "sun.security.util.AuthResources"));
        }
        sid=new String(stringSid);
    }

    public String getName(){
        return sid;
    }

    public int hashCode(){
        return sid.hashCode();
    }

    public boolean equals(Object o){
        if(o==null)
            return false;
        if(this==o)
            return true;
        if(!(o instanceof NTSid))
            return false;
        NTSid that=(NTSid)o;
        if(sid.equals(that.sid)){
            return true;
        }
        return false;
    }

    public String toString(){
        java.text.MessageFormat form=new java.text.MessageFormat
                (sun.security.util.ResourcesMgr.getString
                        ("NTSid.name",
                                "sun.security.util.AuthResources"));
        Object[] source={sid};
        return form.format(source);
    }
}
