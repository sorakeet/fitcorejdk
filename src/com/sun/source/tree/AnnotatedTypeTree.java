/**
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.tree;

import java.util.List;

@jdk.Exported
public interface AnnotatedTypeTree extends ExpressionTree{
    List<? extends AnnotationTree> getAnnotations();

    ExpressionTree getUnderlyingType();
}
