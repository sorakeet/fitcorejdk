/**
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.tree;

import java.util.List;

@jdk.Exported
public interface LambdaExpressionTree extends ExpressionTree{
    List<? extends VariableTree> getParameters();

    Tree getBody();

    BodyKind getBodyKind();

    @jdk.Exported
    public enum BodyKind{
        EXPRESSION,
        STATEMENT;
    }
}
