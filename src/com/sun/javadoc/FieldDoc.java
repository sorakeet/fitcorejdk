/**
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public interface FieldDoc extends MemberDoc{
    Type type();

    boolean isTransient();

    boolean isVolatile();

    SerialFieldTag[] serialFieldTags();

    Object constantValue();

    String constantValueExpression();
}
