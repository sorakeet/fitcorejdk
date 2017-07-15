/**
 * Copyright (c) 2005, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.element;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public interface TypeParameterElement extends Element{
    Element getGenericElement();

    List<? extends TypeMirror> getBounds();

    @Override
    Element getEnclosingElement();
}
