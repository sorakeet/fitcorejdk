/**
 * Copyright (c) 2006, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.tree;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

@jdk.Exported
public interface Scope{
    public Scope getEnclosingScope();

    public TypeElement getEnclosingClass();

    public ExecutableElement getEnclosingMethod();

    public Iterable<? extends Element> getLocalElements();
}
