/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.tree;

import javax.lang.model.type.TypeKind;

@jdk.Exported
public interface PrimitiveTypeTree extends Tree{
    TypeKind getPrimitiveTypeKind();
}
