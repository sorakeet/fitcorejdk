/**
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;

import static javax.lang.model.SourceVersion.RELEASE_7;

@SupportedSourceVersion(RELEASE_7)
public abstract class AbstractAnnotationValueVisitor7<R,P> extends AbstractAnnotationValueVisitor6<R,P>{
    protected AbstractAnnotationValueVisitor7(){
        super();
    }
}
