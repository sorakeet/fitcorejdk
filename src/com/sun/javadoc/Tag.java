/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public interface Tag{
    String name();

    Doc holder();

    String kind();

    String text();

    String toString();

    Tag[] inlineTags();

    Tag[] firstSentenceTags();

    public SourcePosition position();
}
