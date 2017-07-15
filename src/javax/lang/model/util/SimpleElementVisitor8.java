/**
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;

import static javax.lang.model.SourceVersion.RELEASE_8;

@SupportedSourceVersion(RELEASE_8)
public class SimpleElementVisitor8<R,P> extends SimpleElementVisitor7<R,P>{
    protected SimpleElementVisitor8(){
        super(null);
    }

    protected SimpleElementVisitor8(R defaultValue){
        super(defaultValue);
    }
}
