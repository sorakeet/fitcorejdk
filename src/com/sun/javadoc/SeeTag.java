/**
 * Copyright (c) 1998, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public interface SeeTag extends Tag{
    String label();

    public PackageDoc referencedPackage();

    String referencedClassName();

    ClassDoc referencedClass();

    String referencedMemberName();

    MemberDoc referencedMember();
}
