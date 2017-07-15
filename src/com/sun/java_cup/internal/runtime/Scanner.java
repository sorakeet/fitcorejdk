/**
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java_cup.internal.runtime;

public interface Scanner{
    public Symbol next_token() throws Exception;
}
