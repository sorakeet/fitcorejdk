/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.print;

import java.lang.annotation.Native;

public interface Pageable{
    @Native
    int UNKNOWN_NUMBER_OF_PAGES=-1;

    int getNumberOfPages();

    PageFormat getPageFormat(int pageIndex)
            throws IndexOutOfBoundsException;

    Printable getPrintable(int pageIndex)
            throws IndexOutOfBoundsException;
}
