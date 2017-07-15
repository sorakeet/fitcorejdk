/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.type;

import javax.lang.model.element.Element;
import java.util.List;

public interface DeclaredType extends ReferenceType{
    Element asElement();

    TypeMirror getEnclosingType();

    List<? extends TypeMirror> getTypeArguments();
}
