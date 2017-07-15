/**
 * Copyright (c) 2005, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.annotation.processing;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Set;

public interface RoundEnvironment{
    boolean processingOver();

    boolean errorRaised();

    Set<? extends Element> getRootElements();

    Set<? extends Element> getElementsAnnotatedWith(TypeElement a);

    Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a);
}
