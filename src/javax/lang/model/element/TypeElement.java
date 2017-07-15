/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.element;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public interface TypeElement extends Element, Parameterizable, QualifiedNameable{
    NestingKind getNestingKind();

    Name getQualifiedName();

    @Override
    Name getSimpleName();

    @Override
    Element getEnclosingElement();

    @Override
    List<? extends Element> getEnclosedElements();

    TypeMirror getSuperclass();

    List<? extends TypeMirror> getInterfaces();

    List<? extends TypeParameterElement> getTypeParameters();
}
