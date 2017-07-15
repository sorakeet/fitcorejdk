/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.security.auth.module;

@jdk.Exported
public class SolarisSystem{
    protected String username;
    protected long uid;
    protected long gid;
    protected long[] groups;
    public SolarisSystem(){
        System.loadLibrary("jaas_unix");
        getSolarisInfo();
    }

    private native void getSolarisInfo();

    public String getUsername(){
        return username;
    }

    public long getUid(){
        return uid;
    }

    public long getGid(){
        return gid;
    }

    public long[] getGroups(){
        return groups==null?null:groups.clone();
    }
}
