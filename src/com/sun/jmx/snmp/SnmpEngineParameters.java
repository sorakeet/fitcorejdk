/**
 * Copyright (c) 2002, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

import java.io.Serializable;

public class SnmpEngineParameters implements Serializable{
    private static final long serialVersionUID=3720556613478400808L;
    private UserAcl uacl=null;
    private String securityFile=null;
    private boolean encrypt=false;
    private SnmpEngineId engineId=null;

    public String getSecurityFile(){
        return securityFile;
    }

    public void setSecurityFile(String securityFile){
        this.securityFile=securityFile;
    }

    public UserAcl getUserAcl(){
        return uacl;
    }

    public void setUserAcl(UserAcl uacl){
        this.uacl=uacl;
    }

    public void activateEncryption(){
        this.encrypt=true;
    }

    public void deactivateEncryption(){
        this.encrypt=false;
    }

    public boolean isEncryptionEnabled(){
        return encrypt;
    }

    public SnmpEngineId getEngineId(){
        return engineId;
    }

    public void setEngineId(SnmpEngineId engineId){
        this.engineId=engineId;
    }
}
