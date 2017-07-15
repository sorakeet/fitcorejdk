/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.element;

import javax.lang.model.type.DeclaredType;
import java.util.Map;

public interface AnnotationMirror{
    DeclaredType getAnnotationType();

    Map<? extends ExecutableElement,? extends AnnotationValue> getElementValues();
}
