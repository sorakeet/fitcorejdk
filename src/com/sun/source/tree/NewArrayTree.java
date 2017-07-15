/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.tree;

import java.util.List;

@jdk.Exported
public interface NewArrayTree extends ExpressionTree{
    Tree getType();

    List<? extends ExpressionTree> getDimensions();

    List<? extends ExpressionTree> getInitializers();

    List<? extends AnnotationTree> getAnnotations();

    List<? extends List<? extends AnnotationTree>> getDimAnnotations();
}
