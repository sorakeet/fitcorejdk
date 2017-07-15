/**
 * Copyright (c) 1998, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public interface ParamTag extends Tag{
    String parameterName();

    String parameterComment();

    boolean isTypeParameter();
}
