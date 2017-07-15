/**
 * Copyright (c) 2001, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print;

public interface CancelablePrintJob extends DocPrintJob{
    public void cancel() throws PrintException;
}
