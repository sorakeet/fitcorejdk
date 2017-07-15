/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.type;

import java.util.List;

public interface ExecutableType extends TypeMirror{
    List<? extends TypeVariable> getTypeVariables();

    TypeMirror getReturnType();

    List<? extends TypeMirror> getParameterTypes();

    TypeMirror getReceiverType();

    List<? extends TypeMirror> getThrownTypes();
}
