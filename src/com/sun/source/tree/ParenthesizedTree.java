/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.tree;

@jdk.Exported
public interface ParenthesizedTree extends ExpressionTree{
    ExpressionTree getExpression();
}
