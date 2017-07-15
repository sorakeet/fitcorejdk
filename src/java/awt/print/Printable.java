/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.print;

import java.awt.*;

public interface Printable{
    int PAGE_EXISTS=0;
    int NO_SUCH_PAGE=1;

    int print(Graphics graphics,PageFormat pageFormat,int pageIndex)
            throws PrinterException;
}
