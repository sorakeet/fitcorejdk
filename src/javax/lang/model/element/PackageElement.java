/**
 * Copyright (c) 2005, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.element;

import java.util.List;

public interface PackageElement extends Element, QualifiedNameable{
    Name getQualifiedName();

    @Override
    Name getSimpleName();

    @Override
    Element getEnclosingElement();

    @Override
    List<? extends Element> getEnclosedElements();

    boolean isUnnamed();
}
