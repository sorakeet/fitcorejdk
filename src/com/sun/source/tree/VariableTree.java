/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.tree;

import javax.lang.model.element.Name;

@jdk.Exported
public interface VariableTree extends StatementTree{
    ModifiersTree getModifiers();

    Name getName();

    ExpressionTree getNameExpression();

    Tree getType();

    ExpressionTree getInitializer();
}
