/**
 * Copyright (c) 1994, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util;

public interface Enumeration<E>{
    boolean hasMoreElements();

    E nextElement();
}
