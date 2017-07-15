/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.UnknownElementException;

import static javax.lang.model.SourceVersion.RELEASE_6;

@SupportedSourceVersion(RELEASE_6)
public abstract class AbstractElementVisitor6<R,P> implements ElementVisitor<R,P>{
    protected AbstractElementVisitor6(){
    }

    public final R visit(Element e,P p){
        return e.accept(this,p);
    }

    public final R visit(Element e){
        return e.accept(this,null);
    }

    public R visitUnknown(Element e,P p){
        throw new UnknownElementException(e,p);
    }
}
