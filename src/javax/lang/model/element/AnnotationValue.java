/**
 * Copyright (c) 2005, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.element;

public interface AnnotationValue{
    Object getValue();

    String toString();

    <R,P> R accept(AnnotationValueVisitor<R,P> v,P p);
}
