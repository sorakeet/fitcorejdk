/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.acl;

import java.security.Principal;
import java.util.Enumeration;

public interface Group extends Principal{
    public boolean addMember(Principal user);

    public boolean removeMember(Principal user);

    public boolean isMember(Principal member);

    public Enumeration<? extends Principal> members();
}
