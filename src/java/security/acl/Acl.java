/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.acl;

import java.security.Principal;
import java.util.Enumeration;

public interface Acl extends Owner{
    public void setName(Principal caller,String name)
            throws NotOwnerException;

    public String getName();

    public boolean addEntry(Principal caller,AclEntry entry)
            throws NotOwnerException;

    public boolean removeEntry(Principal caller,AclEntry entry)
            throws NotOwnerException;

    public Enumeration<Permission> getPermissions(Principal user);

    public Enumeration<AclEntry> entries();

    public boolean checkPermission(Principal principal,Permission permission);

    public String toString();
}
