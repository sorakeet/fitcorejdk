/**
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.tree;

import java.util.List;

@jdk.Exported
public interface UnionTypeTree extends Tree{
    List<? extends Tree> getTypeAlternatives();
}
