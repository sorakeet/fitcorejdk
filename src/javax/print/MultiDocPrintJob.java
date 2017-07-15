/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print;

import javax.print.attribute.PrintRequestAttributeSet;

public interface MultiDocPrintJob extends DocPrintJob{
    public void print(MultiDoc multiDoc,PrintRequestAttributeSet attributes)
            throws PrintException;
}
