/**
 * Copyright (c) 2002, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.jmx.snmp;
// java import
//

import java.net.InetAddress;
import java.util.Enumeration;

public interface InetAddressAcl{
    public String getName();

    public boolean checkReadPermission(InetAddress address);

    public boolean checkReadPermission(InetAddress address,String community);

    public boolean checkCommunity(String community);

    public boolean checkWritePermission(InetAddress address);

    public boolean checkWritePermission(InetAddress address,String community);

    public Enumeration<InetAddress> getTrapDestinations();

    public Enumeration<String> getTrapCommunities(InetAddress address);

    public Enumeration<InetAddress> getInformDestinations();

    public Enumeration<String> getInformCommunities(InetAddress address);
}
