/**
 * Copyright (c) 1996, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public abstract class Environment{
    public abstract Exception exception();

    public abstract void exception(Exception except);

    public abstract void clear();
}
