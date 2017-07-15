/**
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.type.IntersectionType;

import static javax.lang.model.SourceVersion.RELEASE_8;

@SupportedSourceVersion(RELEASE_8)
public class SimpleTypeVisitor8<R,P> extends SimpleTypeVisitor7<R,P>{
    protected SimpleTypeVisitor8(){
        super(null);
    }

    protected SimpleTypeVisitor8(R defaultValue){
        super(defaultValue);
    }

    @Override
    public R visitIntersection(IntersectionType t,P p){
        return defaultAction(t,p);
    }
}
