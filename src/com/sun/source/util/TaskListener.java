/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.util;

@jdk.Exported
public interface TaskListener{
    public void started(TaskEvent e);

    public void finished(TaskEvent e);
}
