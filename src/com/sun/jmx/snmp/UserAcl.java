/**
 * Copyright (c) 2001, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;
// java import
//

public interface UserAcl{
    public String getName();

    public boolean checkReadPermission(String user);

    public boolean checkReadPermission(String user,String contextName,int securityLevel);

    public boolean checkContextName(String contextName);

    public boolean checkWritePermission(String user);

    public boolean checkWritePermission(String user,String contextName,int securityLevel);
}
