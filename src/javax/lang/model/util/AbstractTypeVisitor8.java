/**
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.util;

import javax.lang.model.type.IntersectionType;

public abstract class AbstractTypeVisitor8<R,P> extends AbstractTypeVisitor7<R,P>{
    protected AbstractTypeVisitor8(){
        super();
    }

    public abstract R visitIntersection(IntersectionType t,P p);
}
