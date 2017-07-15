/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;

import java.io.Serializable;

public abstract class SnmpValue implements Cloneable, Serializable, SnmpDataTypeEnums{
    public String toAsn1String(){
        return "["+getTypeName()+"] "+toString();
    }

    public abstract String getTypeName();

    public abstract SnmpOid toOid();

    public abstract SnmpValue duplicate();

    public boolean isNoSuchObjectValue(){
        return false;
    }

    public boolean isNoSuchInstanceValue(){
        return false;
    }

    public boolean isEndOfMibViewValue(){
        return false;
    }
}
