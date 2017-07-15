/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.util;

import javax.lang.model.element.*;
import java.util.List;
import java.util.Map;

public interface Elements{
    PackageElement getPackageElement(CharSequence name);

    TypeElement getTypeElement(CharSequence name);

    Map<? extends ExecutableElement,? extends AnnotationValue>
    getElementValuesWithDefaults(AnnotationMirror a);

    String getDocComment(Element e);

    boolean isDeprecated(Element e);

    Name getBinaryName(TypeElement type);

    PackageElement getPackageOf(Element type);

    List<? extends Element> getAllMembers(TypeElement type);

    List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e);

    boolean hides(Element hider,Element hidden);

    boolean overrides(ExecutableElement overrider,ExecutableElement overridden,
                      TypeElement type);

    String getConstantExpression(Object value);

    void printElements(java.io.Writer w,Element... elements);

    Name getName(CharSequence cs);

    boolean isFunctionalInterface(TypeElement type);
}
