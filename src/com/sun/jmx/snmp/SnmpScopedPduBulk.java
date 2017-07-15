/**
 * Copyright (c) 2001, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public class SnmpScopedPduBulk extends SnmpScopedPduPacket
        implements SnmpPduBulkType{
    private static final long serialVersionUID=-1648623646227038885L;
    int nonRepeaters;
    int maxRepetitions;

    public SnmpScopedPduBulk(){
        type=pduGetBulkRequestPdu;
        version=snmpVersionThree;
    }

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
    }    public void setMaxRepetitions(int max){
        maxRepetitions=max;
    }

    public void setNonRepeaters(int nr){
        nonRepeaters=nr;
    }

    public int getMaxRepetitions(){
        return maxRepetitions;
    }

    public int getNonRepeaters(){
        return nonRepeaters;
    }


}
