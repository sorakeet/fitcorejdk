/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.util;

import javax.lang.model.type.*;

public abstract class AbstractTypeVisitor6<R,P> implements TypeVisitor<R,P>{
    protected AbstractTypeVisitor6(){
    }

    public final R visit(TypeMirror t,P p){
        return t.accept(this,p);
    }

    public final R visit(TypeMirror t){
        return t.accept(this,null);
    }

    public R visitUnknown(TypeMirror t,P p){
        throw new UnknownTypeException(t,p);
    }

    public R visitUnion(UnionType t,P p){
        return visitUnknown(t,p);
    }

    public R visitIntersection(IntersectionType t,P p){
        return visitUnknown(t,p);
    }
}
