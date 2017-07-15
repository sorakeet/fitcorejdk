/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public interface ExecutableMemberDoc extends MemberDoc{
    ClassDoc[] thrownExceptions();

    Type[] thrownExceptionTypes();

    boolean isNative();

    boolean isSynchronized();

    public boolean isVarArgs();

    Parameter[] parameters();

    Type receiverType();

    ThrowsTag[] throwsTags();

    ParamTag[] paramTags();

    ParamTag[] typeParamTags();

    String signature();

    String flatSignature();

    TypeVariable[] typeParameters();
}
