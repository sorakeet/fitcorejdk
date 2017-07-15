/**
 * Copyright (c) 1998, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;
// java imports
//

import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.SNMP_LOGGER;

public class SnmpMessage extends SnmpMsg implements SnmpDefinitions{
    public byte[] community;

    public int getRequestId(byte[] inputBytes) throws SnmpStatusException{
        int requestId=0;
        BerDecoder bdec=null;
        BerDecoder bdec2=null;
        byte[] any=null;
        try{
            bdec=new BerDecoder(inputBytes);
            bdec.openSequence();
            bdec.fetchInteger();
            bdec.fetchOctetString();
            any=bdec.fetchAny();
            bdec2=new BerDecoder(any);
            int type=bdec2.getTag();
            bdec2.openSequence(type);
            requestId=bdec2.fetchInteger();
        }catch(BerException x){
            throw new SnmpStatusException("Invalid encoding");
        }
        try{
            bdec.closeSequence();
        }catch(BerException x){
        }
        try{
            bdec2.closeSequence();
        }catch(BerException x){
        }
        return requestId;
    }

    public int encodeMessage(byte[] outputBytes) throws SnmpTooBigException{
        int encodingLength=0;
        if(data==null)
            throw new IllegalArgumentException("Data field is null");
        //
        // Reminder: BerEncoder does backward encoding !
        //
        try{
            BerEncoder benc=new BerEncoder(outputBytes);
            benc.openSequence();
            benc.putAny(data,dataLength);
            benc.putOctetString((community!=null)?community:new byte[0]);
            benc.putInteger(version);
            benc.closeSequence();
            encodingLength=benc.trim();
        }catch(ArrayIndexOutOfBoundsException x){
            throw new SnmpTooBigException();
        }
        return encodingLength;
    }

    public void decodeMessage(byte[] inputBytes,int byteCount)
            throws SnmpStatusException{
        try{
            BerDecoder bdec=new BerDecoder(inputBytes/**, byteCount */); // FIXME
            bdec.openSequence();
            version=bdec.fetchInteger();
            community=bdec.fetchOctetString();
            data=bdec.fetchAny();
            dataLength=data.length;
            bdec.closeSequence();
        }catch(BerException x){
            throw new SnmpStatusException("Invalid encoding");
        }
    }

    public void encodeSnmpPdu(SnmpPdu pdu,int maxDataLength)
            throws SnmpStatusException, SnmpTooBigException{
        //
        // The easy work
        //
        SnmpPduPacket pdupacket=(SnmpPduPacket)pdu;
        version=pdupacket.version;
        community=pdupacket.community;
        address=pdupacket.address;
        port=pdupacket.port;
        //
        // Allocate the array to receive the encoding.
        //
        data=new byte[maxDataLength];
        //
        // Encode the pdupacket
        // Reminder: BerEncoder does backward encoding !
        //
        try{
            BerEncoder benc=new BerEncoder(data);
            benc.openSequence();
            encodeVarBindList(benc,pdupacket.varBindList);
            switch(pdupacket.type){
                case pduGetRequestPdu:
                case pduGetNextRequestPdu:
                case pduInformRequestPdu:
                case pduGetResponsePdu:
                case pduSetRequestPdu:
                case pduV2TrapPdu:
                case pduReportPdu:
                    SnmpPduRequest reqPdu=(SnmpPduRequest)pdupacket;
                    benc.putInteger(reqPdu.errorIndex);
                    benc.putInteger(reqPdu.errorStatus);
                    benc.putInteger(reqPdu.requestId);
                    break;
                case pduGetBulkRequestPdu:
                    SnmpPduBulk bulkPdu=(SnmpPduBulk)pdupacket;
                    benc.putInteger(bulkPdu.maxRepetitions);
                    benc.putInteger(bulkPdu.nonRepeaters);
                    benc.putInteger(bulkPdu.requestId);
                    break;
                case pduV1TrapPdu:
                    SnmpPduTrap trapPdu=(SnmpPduTrap)pdupacket;
                    benc.putInteger(trapPdu.timeStamp,SnmpValue.TimeticksTag);
                    benc.putInteger(trapPdu.specificTrap);
                    benc.putInteger(trapPdu.genericTrap);
                    if(trapPdu.agentAddr!=null)
                        benc.putOctetString(trapPdu.agentAddr.byteValue(),SnmpValue.IpAddressTag);
                    else
                        benc.putOctetString(new byte[0],SnmpValue.IpAddressTag);
                    benc.putOid(trapPdu.enterprise.longValue());
                    break;
                default:
                    throw new SnmpStatusException("Invalid pdu type "+String.valueOf(pdupacket.type));
            }
            benc.closeSequence(pdupacket.type);
            dataLength=benc.trim();
        }catch(ArrayIndexOutOfBoundsException x){
            throw new SnmpTooBigException();
        }
    }

    public SnmpPdu decodeSnmpPdu()
            throws SnmpStatusException{
        //
        // Decode the pdu
        //
        SnmpPduPacket pdu=null;
        BerDecoder bdec=new BerDecoder(data);
        try{
            int type=bdec.getTag();
            bdec.openSequence(type);
            switch(type){
                case pduGetRequestPdu:
                case pduGetNextRequestPdu:
                case pduInformRequestPdu:
                case pduGetResponsePdu:
                case pduSetRequestPdu:
                case pduV2TrapPdu:
                case pduReportPdu:
                    SnmpPduRequest reqPdu=new SnmpPduRequest();
                    reqPdu.requestId=bdec.fetchInteger();
                    reqPdu.errorStatus=bdec.fetchInteger();
                    reqPdu.errorIndex=bdec.fetchInteger();
                    pdu=reqPdu;
                    break;
                case pduGetBulkRequestPdu:
                    SnmpPduBulk bulkPdu=new SnmpPduBulk();
                    bulkPdu.requestId=bdec.fetchInteger();
                    bulkPdu.nonRepeaters=bdec.fetchInteger();
                    bulkPdu.maxRepetitions=bdec.fetchInteger();
                    pdu=bulkPdu;
                    break;
                case pduV1TrapPdu:
                    SnmpPduTrap trapPdu=new SnmpPduTrap();
                    trapPdu.enterprise=new SnmpOid(bdec.fetchOid());
                    byte[] b=bdec.fetchOctetString(SnmpValue.IpAddressTag);
                    if(b.length!=0)
                        trapPdu.agentAddr=new SnmpIpAddress(b);
                    else
                        trapPdu.agentAddr=null;
                    trapPdu.genericTrap=bdec.fetchInteger();
                    trapPdu.specificTrap=bdec.fetchInteger();
                    trapPdu.timeStamp=bdec.fetchInteger(SnmpValue.TimeticksTag);
                    pdu=trapPdu;
                    break;
                default:
                    throw new SnmpStatusException(snmpRspWrongEncoding);
            }
            pdu.type=type;
            pdu.varBindList=decodeVarBindList(bdec);
            bdec.closeSequence();
        }catch(BerException e){
            if(SNMP_LOGGER.isLoggable(Level.FINEST)){
                SNMP_LOGGER.logp(Level.FINEST,SnmpMessage.class.getName(),
                        "decodeSnmpPdu","BerException",e);
            }
            throw new SnmpStatusException(snmpRspWrongEncoding);
        }catch(IllegalArgumentException e){
            // bug id 4654066
            if(SNMP_LOGGER.isLoggable(Level.FINEST)){
                SNMP_LOGGER.logp(Level.FINEST,SnmpMessage.class.getName(),
                        "decodeSnmpPdu","IllegalArgumentException",e);
            }
            throw new SnmpStatusException(snmpRspWrongEncoding);
        }
        //
        // The easy work
        //
        pdu.version=version;
        pdu.community=community;
        pdu.address=address;
        pdu.port=port;
        return pdu;
    }

    public String printMessage(){
        StringBuffer sb=new StringBuffer();
        if(community==null){
            sb.append("Community: null");
        }else{
            sb.append("Community: {\n");
            sb.append(dumpHexBuffer(community,0,community.length));
            sb.append("\n}\n");
        }
        return sb.append(super.printMessage()).toString();
    }
}
