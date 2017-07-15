/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Copyright (c) 1995-96 by Cisco Systems, Inc.
package com.sun.jmx.snmp;

public interface SnmpDataTypeEnums{
    // ASN1 Type definitions.
    //-----------------------
    static public final int BooleanTag=1;
    static public final int IntegerTag=2;
    static public final int BitStringTag=2;
    static public final int OctetStringTag=4;
    static public final int NullTag=5;
    static public final int ObjectIdentiferTag=6;
    final public static int UnknownSyntaxTag=0xFF;
    final public static int SequenceTag=0x30;
    final public static int TableTag=0xFE;
    // SNMP definitions.
    //------------------
    static public final int ApplFlag=64;
    static public final int CtxtFlag=128;
    static public final int IpAddressTag=ApplFlag|0;
    static public final int CounterTag=ApplFlag|1;
    static public final int GaugeTag=ApplFlag|2;
    static public final int TimeticksTag=ApplFlag|3;
    static public final int OpaqueTag=ApplFlag|4;
    static public final int Counter64Tag=ApplFlag|6;
    static final public int NsapTag=ApplFlag|5;
    static final public int UintegerTag=ApplFlag|7;
    static final public int errNoSuchObjectTag=CtxtFlag|0;
    static final public int errNoSuchInstanceTag=CtxtFlag|1;
    static final public int errEndOfMibViewTag=CtxtFlag|2;
}
