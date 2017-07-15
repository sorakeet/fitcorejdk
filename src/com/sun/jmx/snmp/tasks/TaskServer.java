// NPCTE fix for bugId 4510777, esc 532372, MR October 2001
// file TaskServer.java created for this bug fix
/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp.tasks;

public interface TaskServer{
    public void submitTask(Task task);
}
