/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.tree;

import java.util.List;

@jdk.Exported
public interface SwitchTree extends StatementTree{
    ExpressionTree getExpression();

    List<? extends CaseTree> getCases();
}
