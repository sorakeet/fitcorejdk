/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.event;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

public interface EventContext extends Context{
    public final static int OBJECT_SCOPE=0;
    public final static int ONELEVEL_SCOPE=1;
    public final static int SUBTREE_SCOPE=2;

    void addNamingListener(Name target,int scope,NamingListener l)
            throws NamingException;

    void addNamingListener(String target,int scope,NamingListener l)
            throws NamingException;

    void removeNamingListener(NamingListener l) throws NamingException;

    boolean targetMustExist() throws NamingException;
}
