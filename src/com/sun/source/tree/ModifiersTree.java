/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.tree;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Set;

@jdk.Exported
public interface ModifiersTree extends Tree{
    Set<Modifier> getFlags();

    List<? extends AnnotationTree> getAnnotations();
}
