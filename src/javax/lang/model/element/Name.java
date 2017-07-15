/**
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.element;

public interface Name extends CharSequence{
    int hashCode();

    boolean equals(Object obj);

    boolean contentEquals(CharSequence cs);
}
