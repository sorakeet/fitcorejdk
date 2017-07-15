/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

public interface LdapContext extends DirContext{
    static final String CONTROL_FACTORIES="java.naming.factory.control";

    public ExtendedResponse extendedOperation(ExtendedRequest request)
            throws NamingException;

    public LdapContext newInstance(Control[] requestControls)
            throws NamingException;

    public void reconnect(Control[] connCtls) throws NamingException;

    public Control[] getConnectControls() throws NamingException;

    public Control[] getRequestControls() throws NamingException;

    public void setRequestControls(Control[] requestControls)
            throws NamingException;

    public Control[] getResponseControls() throws NamingException;
}
