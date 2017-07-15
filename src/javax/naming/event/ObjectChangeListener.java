/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.event;

public interface ObjectChangeListener extends NamingListener{
    void objectChanged(NamingEvent evt);
}
