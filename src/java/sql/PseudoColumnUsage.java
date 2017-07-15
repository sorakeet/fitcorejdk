/**
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

public enum PseudoColumnUsage{
    SELECT_LIST_ONLY,
    WHERE_CLAUSE_ONLY,
    NO_USAGE_RESTRICTIONS,
    USAGE_UNKNOWN
}
