/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.type;

public interface TypeMirror extends javax.lang.model.AnnotatedConstruct{
    TypeKind getKind();

    int hashCode();

    boolean equals(Object obj);

    String toString();

    <R,P> R accept(TypeVisitor<R,P> v,P p);
}
