/**
 * Copyright (c) 2006, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.tree;

@jdk.Exported
public interface LineMap{
    long getStartPosition(long line);

    long getPosition(long line,long column);

    long getLineNumber(long pos);

    long getColumnNumber(long pos);
}
