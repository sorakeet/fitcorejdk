/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.security.acl;

import java.security.Principal;

public interface Owner{
    public boolean addOwner(Principal caller,Principal owner)
            throws NotOwnerException;

    public boolean deleteOwner(Principal caller,Principal owner)
            throws NotOwnerException, LastOwnerException;

    public boolean isOwner(Principal owner);
}
