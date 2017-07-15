/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.util;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import java.util.List;

public interface Types{
    Element asElement(TypeMirror t);

    boolean isSameType(TypeMirror t1,TypeMirror t2);

    boolean isSubtype(TypeMirror t1,TypeMirror t2);

    boolean isAssignable(TypeMirror t1,TypeMirror t2);

    boolean contains(TypeMirror t1,TypeMirror t2);

    boolean isSubsignature(ExecutableType m1,ExecutableType m2);

    List<? extends TypeMirror> directSupertypes(TypeMirror t);

    TypeMirror erasure(TypeMirror t);

    TypeElement boxedClass(PrimitiveType p);

    PrimitiveType unboxedType(TypeMirror t);

    TypeMirror capture(TypeMirror t);

    PrimitiveType getPrimitiveType(TypeKind kind);

    NullType getNullType();

    NoType getNoType(TypeKind kind);

    ArrayType getArrayType(TypeMirror componentType);

    WildcardType getWildcardType(TypeMirror extendsBound,
                                 TypeMirror superBound);

    DeclaredType getDeclaredType(TypeElement typeElem,TypeMirror... typeArgs);

    DeclaredType getDeclaredType(DeclaredType containing,
                                 TypeElement typeElem,TypeMirror... typeArgs);

    TypeMirror asMemberOf(DeclaredType containing,Element element);
}
