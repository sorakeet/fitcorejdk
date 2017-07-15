/**
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public interface ProgramElementDoc extends Doc{
    ClassDoc containingClass();

    PackageDoc containingPackage();

    String qualifiedName();

    int modifierSpecifier();

    String modifiers();

    AnnotationDesc[] annotations();

    boolean isPublic();

    boolean isProtected();

    boolean isPrivate();

    boolean isPackagePrivate();

    boolean isStatic();

    boolean isFinal();
}
