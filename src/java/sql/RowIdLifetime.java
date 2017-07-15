/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

public enum RowIdLifetime{
    ROWID_UNSUPPORTED,
    ROWID_VALID_OTHER,
    ROWID_VALID_SESSION,
    ROWID_VALID_TRANSACTION,
    ROWID_VALID_FOREVER
}
