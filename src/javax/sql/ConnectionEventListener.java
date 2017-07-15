/**
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql;

public interface ConnectionEventListener extends java.util.EventListener{
    void connectionClosed(ConnectionEvent event);

    void connectionErrorOccurred(ConnectionEvent event);
}
