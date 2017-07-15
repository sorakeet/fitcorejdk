/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

public interface RowId{
    byte[] getBytes();

    int hashCode();

    boolean equals(Object obj);

    String toString();
}
