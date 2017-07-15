/**
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.doctree;

@jdk.Exported
public interface LiteralTree extends InlineTagTree{
    TextTree getBody();
}
