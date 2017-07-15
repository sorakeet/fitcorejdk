/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

public interface SnmpPduBulkType extends SnmpAckPdu{
    public int getMaxRepetitions();

    public void setMaxRepetitions(int max);

    public int getNonRepeaters();

    public void setNonRepeaters(int nr);
}
