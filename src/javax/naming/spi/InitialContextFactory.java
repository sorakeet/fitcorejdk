/**
 * Copyright (c) 1999, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.spi;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.Hashtable;

public interface InitialContextFactory{
    public Context getInitialContext(Hashtable<?,?> environment)
            throws NamingException;
}
