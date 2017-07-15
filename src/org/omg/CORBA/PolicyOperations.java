/**
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

public interface PolicyOperations{
    int policy_type();

    Policy copy();

    void destroy();
} // interface PolicyOperations
