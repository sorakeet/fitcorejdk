/**
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.doctree;

import javax.lang.model.element.Name;
import java.util.List;

@jdk.Exported
public interface AttributeTree extends DocTree{
    Name getName();

    ;

    ValueKind getValueKind();

    List<? extends DocTree> getValue();

    @jdk.Exported
    enum ValueKind{
        EMPTY,UNQUOTED,SINGLE,DOUBLE
    }
}
