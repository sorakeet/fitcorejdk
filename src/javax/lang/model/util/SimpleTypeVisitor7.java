/**
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.type.UnionType;

import static javax.lang.model.SourceVersion.RELEASE_7;

@SupportedSourceVersion(RELEASE_7)
public class SimpleTypeVisitor7<R,P> extends SimpleTypeVisitor6<R,P>{
    protected SimpleTypeVisitor7(){
        super(null);
    }

    protected SimpleTypeVisitor7(R defaultValue){
        super(defaultValue);
    }

    @Override
    public R visitUnion(UnionType t,P p){
        return defaultAction(t,p);
    }
}
