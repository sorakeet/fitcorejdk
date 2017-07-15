/**
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.doctree;

import javax.lang.model.element.Name;
import java.util.List;

@jdk.Exported
public interface StartElementTree extends DocTree{
    Name getName();

    List<? extends DocTree> getAttributes();

    boolean isSelfClosing();
}
