/**
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public interface RootDoc extends Doc, DocErrorReporter{
    String[][] options();

    PackageDoc[] specifiedPackages();

    ClassDoc[] specifiedClasses();

    ClassDoc[] classes();

    PackageDoc packageNamed(String name);

    ClassDoc classNamed(String qualifiedName);
}
