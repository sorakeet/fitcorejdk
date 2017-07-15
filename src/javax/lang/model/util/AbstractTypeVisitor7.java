/**
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.util;

import javax.lang.model.type.UnionType;

public abstract class AbstractTypeVisitor7<R,P> extends AbstractTypeVisitor6<R,P>{
    protected AbstractTypeVisitor7(){
        super();
    }

    public abstract R visitUnion(UnionType t,P p);
}
