/**
 * Copyright (c) 1999, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ReferralException;
import java.util.Hashtable;

public abstract class LdapReferralException extends ReferralException{
    private static final long serialVersionUID=-1668992791764950804L;

    protected LdapReferralException(String explanation){
        super(explanation);
    }

    protected LdapReferralException(){
        super();
    }

    public abstract Context getReferralContext() throws NamingException;

    public abstract Context
    getReferralContext(Hashtable<?,?> env)
            throws NamingException;

    public abstract Context
    getReferralContext(Hashtable<?,?> env,
                       Control[] reqCtls)
            throws NamingException;
}
