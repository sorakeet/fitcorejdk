/**
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.tree;

import javax.lang.model.element.Name;
import java.util.List;

@jdk.Exported
public interface MemberReferenceTree extends ExpressionTree{
    ReferenceMode getMode();

    ExpressionTree getQualifierExpression();

    Name getName();

    List<? extends ExpressionTree> getTypeArguments();

    @jdk.Exported
    public enum ReferenceMode{
        INVOKE,
        NEW
    }
}
