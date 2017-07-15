/**
 * Copyright (c) 1998, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.javadoc;

public interface Doc extends Comparable<Object>{
    String commentText();

    Tag[] tags();

    Tag[] tags(String tagname);

    SeeTag[] seeTags();

    Tag[] inlineTags();

    Tag[] firstSentenceTags();

    String getRawCommentText();

    void setRawCommentText(String rawDocumentation);

    String name();

    int compareTo(Object obj);

    boolean isField();

    boolean isEnumConstant();

    boolean isConstructor();

    boolean isMethod();

    boolean isAnnotationTypeElement();

    boolean isInterface();

    boolean isException();

    boolean isError();

    boolean isEnum();

    boolean isAnnotationType();

    boolean isOrdinaryClass();

    boolean isClass();

    boolean isIncluded();

    SourcePosition position();
}
