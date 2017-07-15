/**
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;
// java imports
//

import java.io.Serializable;
// jmx import
//
// SNMP Runtime import
//

public class SnmpPduFactoryBER implements SnmpPduFactory, Serializable{
    private static final long serialVersionUID=-3525318344000547635L;

    public SnmpPdu decodeSnmpPdu(SnmpMsg msg) throws SnmpStatusException{
        return msg.decodeSnmpPdu();
    }

    public SnmpMsg encodeSnmpPdu(SnmpPdu p,int maxDataLength)
            throws SnmpStatusException, SnmpTooBigException{
        switch(p.version){
            case SnmpDefinitions.snmpVersionOne:
            case SnmpDefinitions.snmpVersionTwo:{
                SnmpMessage result=new SnmpMessage();
                result.encodeSnmpPdu((SnmpPduPacket)p,maxDataLength);
                return result;
            }
            case SnmpDefinitions.snmpVersionThree:{
                SnmpV3Message result=new SnmpV3Message();
                result.encodeSnmpPdu(p,maxDataLength);
                return result;
            }
            default:
                return null;
        }
    }
}
