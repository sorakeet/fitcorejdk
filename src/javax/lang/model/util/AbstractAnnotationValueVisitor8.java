/**
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;

import static javax.lang.model.SourceVersion.RELEASE_8;

@SupportedSourceVersion(RELEASE_8)
public abstract class AbstractAnnotationValueVisitor8<R,P> extends AbstractAnnotationValueVisitor7<R,P>{
    protected AbstractAnnotationValueVisitor8(){
        super();
    }
}
