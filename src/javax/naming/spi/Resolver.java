/**
 * Copyright (c) 1999, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.spi;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

public interface Resolver{
    public ResolveResult resolveToClass(Name name,
                                        Class<? extends Context> contextType)
            throws NamingException;

    public ResolveResult resolveToClass(String name,
                                        Class<? extends Context> contextType)
            throws NamingException;
};
