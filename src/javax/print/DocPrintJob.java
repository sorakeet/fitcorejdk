/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print;

import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobListener;

public interface DocPrintJob{
    public PrintService getPrintService();

    public PrintJobAttributeSet getAttributes();

    public void addPrintJobListener(PrintJobListener listener);

    public void removePrintJobListener(PrintJobListener listener);

    public void addPrintJobAttributeListener(
            PrintJobAttributeListener listener,
            PrintJobAttributeSet attributes);

    public void removePrintJobAttributeListener(
            PrintJobAttributeListener listener);

    public void print(Doc doc,PrintRequestAttributeSet attributes)
            throws PrintException;
}
