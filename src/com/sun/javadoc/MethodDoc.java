/**
 * Copyright (c) 1998, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public interface MethodDoc extends ExecutableMemberDoc{
    boolean isAbstract();

    boolean isDefault();

    Type returnType();

    ClassDoc overriddenClass();

    Type overriddenType();

    MethodDoc overriddenMethod();

    boolean overrides(MethodDoc meth);
}
