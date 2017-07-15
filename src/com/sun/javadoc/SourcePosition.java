/**
 * Copyright (c) 2001, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

import java.io.File;

public interface SourcePosition{
    File file();

    int line();

    int column();

    String toString();
}
