/**
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.doctree;

import java.util.List;

@jdk.Exported
public interface DocCommentTree extends DocTree{
    List<? extends DocTree> getFirstSentence();

    List<? extends DocTree> getBody();

    List<? extends DocTree> getBlockTags();
}
