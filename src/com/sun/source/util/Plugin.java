/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.util;

@jdk.Exported
public interface Plugin{
    String getName();

    void init(JavacTask task,String... args);
}
