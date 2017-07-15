/**
 * Copyright (c) 1998, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.agent;
// jmx imports
//

import javax.management.Notification;
import javax.management.ObjectName;

public class SnmpTableEntryNotification extends Notification{
    // PUBLIC VARIABLES
    //-----------------
    public static final String SNMP_ENTRY_ADDED=
            "jmx.snmp.table.entry.added";
    public static final String SNMP_ENTRY_REMOVED=
            "jmx.snmp.table.entry.removed";
    // Ensure compatibility
    //
    private static final long serialVersionUID=5832592016227890252L;
    // PRIVATE VARIABLES
    //------------------
    private final Object entry;
    private final ObjectName name;
    SnmpTableEntryNotification(String type,Object source,
                               long sequenceNumber,long timeStamp,
                               Object entry,ObjectName entryName){
        super(type,source,sequenceNumber,timeStamp);
        this.entry=entry;
        this.name=entryName;
    }

    public Object getEntry(){
        return entry;
    }

    public ObjectName getEntryName(){
        return name;
    }
}
