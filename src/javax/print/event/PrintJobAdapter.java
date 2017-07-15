/**
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.event;

public abstract class PrintJobAdapter implements PrintJobListener{
    public void printDataTransferCompleted(PrintJobEvent pje){
    }

    public void printJobCompleted(PrintJobEvent pje){
    }

    public void printJobFailed(PrintJobEvent pje){
    }

    public void printJobCanceled(PrintJobEvent pje){
    }

    public void printJobNoMoreEvents(PrintJobEvent pje){
    }

    public void printJobRequiresAttention(PrintJobEvent pje){
    }
}
