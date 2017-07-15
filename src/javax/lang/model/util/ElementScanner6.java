/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.*;

import static javax.lang.model.SourceVersion.RELEASE_6;

@SupportedSourceVersion(RELEASE_6)
public class ElementScanner6<R,P> extends AbstractElementVisitor6<R,P>{
    protected final R DEFAULT_VALUE;

    protected ElementScanner6(){
        DEFAULT_VALUE=null;
    }

    protected ElementScanner6(R defaultValue){
        DEFAULT_VALUE=defaultValue;
    }

    public final R scan(Element e){
        return scan(e,null);
    }

    public R scan(Element e,P p){
        return e.accept(this,p);
    }

    public R visitPackage(PackageElement e,P p){
        return scan(e.getEnclosedElements(),p);
    }

    public final R scan(Iterable<? extends Element> iterable,P p){
        R result=DEFAULT_VALUE;
        for(Element e : iterable)
            result=scan(e,p);
        return result;
    }

    public R visitType(TypeElement e,P p){
        return scan(e.getEnclosedElements(),p);
    }

    public R visitVariable(VariableElement e,P p){
        if(e.getKind()!=ElementKind.RESOURCE_VARIABLE)
            return scan(e.getEnclosedElements(),p);
        else
            return visitUnknown(e,p);
    }

    public R visitExecutable(ExecutableElement e,P p){
        return scan(e.getParameters(),p);
    }

    public R visitTypeParameter(TypeParameterElement e,P p){
        return scan(e.getEnclosedElements(),p);
    }
}
