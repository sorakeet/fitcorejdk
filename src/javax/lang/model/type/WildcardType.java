/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.type;

public interface WildcardType extends TypeMirror{
    TypeMirror getExtendsBound();

    TypeMirror getSuperBound();
}
