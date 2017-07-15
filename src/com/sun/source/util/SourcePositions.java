/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.util;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;

@jdk.Exported
public interface SourcePositions{
    long getStartPosition(CompilationUnitTree file,Tree tree);

    long getEndPosition(CompilationUnitTree file,Tree tree);
}
