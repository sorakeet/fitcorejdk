/**
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public interface PackageDoc extends Doc{
    ClassDoc[] allClasses(boolean filter);

    ClassDoc[] allClasses();

    ClassDoc[] ordinaryClasses();

    ClassDoc[] exceptions();

    ClassDoc[] errors();

    ClassDoc[] enums();

    ClassDoc[] interfaces();

    AnnotationTypeDoc[] annotationTypes();

    AnnotationDesc[] annotations();

    ClassDoc findClass(String className);
}
