/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Copyright (c) 1995-96 by Cisco Systems, Inc.
package com.sun.jmx.snmp;

public interface SnmpDefinitions{
    public static final int noAuthNoPriv=0;
    public static final int authNoPriv=1;
    public static final int authPriv=3;
    public static final int reportableFlag=4;
    public static final int authMask=1;
    public static final int privMask=2;
    public static final int authPrivMask=3;
    public final static int CtxtConsFlag=128|32;
    final public static byte snmpVersionOne=0;
    final public static byte snmpVersionTwo=1;
    final public static byte snmpVersionThree=3;
    public final static int pduGetRequestPdu=CtxtConsFlag|0;
    public final static int pduGetNextRequestPdu=CtxtConsFlag|1;
    public final static int pduGetResponsePdu=CtxtConsFlag|2;
    public final static int pduSetRequestPdu=CtxtConsFlag|3;
    public final static int pduGetBulkRequestPdu=CtxtConsFlag|5;
    public final static int pduWalkRequest=0xFD;
    public final static int pduV1TrapPdu=CtxtConsFlag|4;
    public final static int pduV2TrapPdu=CtxtConsFlag|7;
    public final static int pduInformRequestPdu=CtxtConsFlag|6;
    public final static int pduReportPdu=CtxtConsFlag|8;
    // SNMPv1 values for generic trap info in Trap-PDU.
    //-------------------------------------------------
    public static final int trapColdStart=0;
    public static final int trapWarmStart=1;
    public static final int trapLinkDown=2;
    public static final int trapLinkUp=3;
    public static final int trapAuthenticationFailure=4;
    public static final int trapEgpNeighborLoss=5;
    public static final int trapEnterpriseSpecific=6;
    // PDU error status enumeration.
    //------------------------------
    final public static int snmpRspNoError=0;
    final public static int snmpRspTooBig=1;
    final public static int snmpRspNoSuchName=2;
    final public static int snmpRspBadValue=3;
    final public static int snmpRspReadOnly=4;
    final public static int snmpRspGenErr=5;
    final public static int snmpRspNoAccess=6;
    final public static int snmpRspWrongType=7;
    final public static int snmpRspWrongLength=8;
    final public static int snmpRspWrongEncoding=9;
    final public static int snmpRspWrongValue=10;
    final public static int snmpRspNoCreation=11;
    final public static int snmpRspInconsistentValue=12;
    final public static int snmpRspResourceUnavailable=13;
    final public static int snmpRspCommitFailed=14;
    final public static int snmpRspUndoFailed=15;
    final public static int snmpRspAuthorizationError=16;
    final public static int snmpRspNotWritable=17;
    final public static int snmpRspInconsistentName=18;
    final public static int noSuchView=19;
    final public static int noSuchContext=20;
    final public static int noGroupName=21;
    final public static int notInView=22;
    // API error status enumeration.
    //------------------------------
    final public static int snmpReqTimeout=(0xE0);
    final public static int snmpReqAborted=(0xE1);
    final public static int snmpRspDecodingError=(0xE2);
    final public static int snmpReqEncodingError=(0xE3);
    final public static int snmpReqPacketOverflow=(0xE4);
    final public static int snmpRspEndOfTable=(0xE5);
    final public static int snmpReqRefireAfterVbFix=(0xE6);
    final public static int snmpReqHandleTooBig=(0xE7);
    final public static int snmpReqTooBigImpossible=(0xE8);
    final public static int snmpReqInternalError=(0xF0);
    final public static int snmpReqSocketIOError=(0xF1);
    final public static int snmpReqUnknownError=(0xF2);
    final public static int snmpWrongSnmpVersion=(0xF3);
    final public static int snmpUnknownPrincipal=(0xF4);
    final public static int snmpAuthNotSupported=(0xF5);
    final public static int snmpPrivNotSupported=(0xF6);
    final public static int snmpBadSecurityLevel=(0xF9);
    final public static int snmpUsmBadEngineId=(0xF7);
    final public static int snmpUsmInvalidTimeliness=(0xF8);
    final public static int snmpV1SecurityModel=1;
    final public static int snmpV2SecurityModel=2;
    final public static int snmpUsmSecurityModel=3;
    final public static int snmpV1MsgProcessingModel=snmpVersionOne;
    final public static int snmpV2MsgProcessingModel=snmpVersionTwo;
    final public static int snmpV3MsgProcessingModel=snmpVersionThree;
    final public static int snmpV1AccessControlModel=snmpVersionOne;
    final public static int snmpV2AccessControlModel=snmpVersionTwo;
    final public static int snmpV3AccessControlModel=snmpVersionThree;
}
