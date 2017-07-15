/**
 * Copyright (c) 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA.portable;

public interface ValueOutputStream{
    void start_value(String rep_id);

    void end_value();
}
