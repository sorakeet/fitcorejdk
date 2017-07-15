/**
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public interface DocErrorReporter{
    void printError(String msg);

    void printError(SourcePosition pos,String msg);

    void printWarning(String msg);

    void printWarning(SourcePosition pos,String msg);

    void printNotice(String msg);

    void printNotice(SourcePosition pos,String msg);
}
