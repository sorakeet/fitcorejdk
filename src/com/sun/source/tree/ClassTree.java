/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.tree;

import javax.lang.model.element.Name;
import java.util.List;

@jdk.Exported
public interface ClassTree extends StatementTree{
    ModifiersTree getModifiers();

    Name getSimpleName();

    List<? extends TypeParameterTree> getTypeParameters();

    Tree getExtendsClause();

    List<? extends Tree> getImplementsClause();

    List<? extends Tree> getMembers();
}
