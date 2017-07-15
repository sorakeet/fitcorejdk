/**
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql;

public interface RowSetListener extends java.util.EventListener{
    void rowSetChanged(RowSetEvent event);

    void rowChanged(RowSetEvent event);

    void cursorMoved(RowSetEvent event);
}
