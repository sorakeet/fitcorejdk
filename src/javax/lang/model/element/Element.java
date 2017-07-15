/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.element;

import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

public interface Element extends javax.lang.model.AnnotatedConstruct{
    TypeMirror asType();

    ElementKind getKind();

    Set<Modifier> getModifiers();

    Name getSimpleName();

    Element getEnclosingElement();

    List<? extends Element> getEnclosedElements();

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);

    @Override
    List<? extends AnnotationMirror> getAnnotationMirrors();

    @Override
    <A extends Annotation> A getAnnotation(Class<A> annotationType);

    <R,P> R accept(ElementVisitor<R,P> v,P p);
}
