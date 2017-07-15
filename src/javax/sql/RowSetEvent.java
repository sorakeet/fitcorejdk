/**
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql;

public class RowSetEvent extends java.util.EventObject{
    static final long serialVersionUID=-1875450876546332005L;

    public RowSetEvent(RowSet source){
        super(source);
    }
}
