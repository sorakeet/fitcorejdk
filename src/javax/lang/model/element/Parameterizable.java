/**
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.element;

import java.util.List;

public interface Parameterizable extends Element{
    List<? extends TypeParameterElement> getTypeParameters();
}
