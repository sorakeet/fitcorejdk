/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

import java.util.Hashtable;

public abstract class ReferralException extends NamingException{
    private static final long serialVersionUID=-2881363844695698876L;

    protected ReferralException(String explanation){
        super(explanation);
    }

    protected ReferralException(){
        super();
    }

    public abstract Object getReferralInfo();

    public abstract Context getReferralContext() throws NamingException;

    public abstract Context
    getReferralContext(Hashtable<?,?> env)
            throws NamingException;

    public abstract boolean skipReferral();

    public abstract void retryReferral();
}
