/**
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.doctree;

import java.util.List;

@jdk.Exported
public interface SerialTree extends BlockTagTree{
    List<? extends DocTree> getDescription();
}
