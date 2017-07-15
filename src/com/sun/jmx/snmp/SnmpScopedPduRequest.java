/**
 * Copyright (c) 2001, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class SnmpScopedPduRequest extends SnmpScopedPduPacket
        implements SnmpPduRequestType{
    private static final long serialVersionUID=6463060973056773680L;
    int errorStatus=0;
    int errorIndex=0;

    public SnmpPdu getResponsePdu(){
        SnmpScopedPduRequest result=new SnmpScopedPduRequest();
        result.address=address;
        result.port=port;
        result.version=version;
        result.requestId=requestId;
        result.msgId=msgId;
        result.msgMaxSize=msgMaxSize;
        result.msgFlags=msgFlags;
        result.msgSecurityModel=msgSecurityModel;
        result.contextEngineId=contextEngineId;
        result.contextName=contextName;
        result.securityParameters=securityParameters;
        result.type=pduGetResponsePdu;
        result.errorStatus=SnmpDefinitions.snmpRspNoError;
        result.errorIndex=0;
        return result;
    }    public void setErrorIndex(int i){
        errorIndex=i;
    }

    public void setErrorStatus(int s){
        errorStatus=s;
    }

    public int getErrorIndex(){
        return errorIndex;
    }

    public int getErrorStatus(){
        return errorStatus;
    }


}
