/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.UnknownAnnotationValueException;

import static javax.lang.model.SourceVersion.RELEASE_6;

@SupportedSourceVersion(RELEASE_6)
public abstract class AbstractAnnotationValueVisitor6<R,P>
        implements AnnotationValueVisitor<R,P>{
    protected AbstractAnnotationValueVisitor6(){
    }

    public final R visit(AnnotationValue av,P p){
        return av.accept(this,p);
    }

    public final R visit(AnnotationValue av){
        return av.accept(this,null);
    }

    public R visitUnknown(AnnotationValue av,P p){
        throw new UnknownAnnotationValueException(av,p);
    }
}
