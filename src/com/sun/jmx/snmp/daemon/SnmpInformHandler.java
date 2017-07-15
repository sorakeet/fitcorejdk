/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.daemon;
// JMX imports
//

import com.sun.jmx.snmp.SnmpDefinitions;
import com.sun.jmx.snmp.SnmpVarBindList;

public interface SnmpInformHandler extends SnmpDefinitions{
    public abstract void processSnmpPollData(SnmpInformRequest request,int errStatus,int errIndex,SnmpVarBindList vblist);

    public abstract void processSnmpPollTimeout(SnmpInformRequest request);

    public abstract void processSnmpInternalError(SnmpInformRequest request,String errmsg);
}
