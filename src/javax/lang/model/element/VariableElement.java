/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.element;

public interface VariableElement extends Element{
    Object getConstantValue();

    @Override
    Name getSimpleName();

    @Override
    Element getEnclosingElement();
}
