/**
 * Copyright (c) 1999, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

import javax.naming.NamingException;

public interface UnsolicitedNotification extends ExtendedResponse, HasControls{
    public String[] getReferrals();

    public NamingException getException();
}
