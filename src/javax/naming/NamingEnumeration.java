/**
 * Copyright (c) 1999, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

import java.util.Enumeration;

public interface NamingEnumeration<T> extends Enumeration<T>{
    public T next() throws NamingException;

    public boolean hasMore() throws NamingException;

    public void close() throws NamingException;
}
