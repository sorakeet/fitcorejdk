/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.event;

import javax.print.DocPrintJob;

public class PrintJobEvent extends PrintEvent{
    public static final int JOB_CANCELED=101;
    public static final int JOB_COMPLETE=102;
    public static final int JOB_FAILED=103;
    public static final int REQUIRES_ATTENTION=104;
    public static final int NO_MORE_EVENTS=105;
    public static final int DATA_TRANSFER_COMPLETE=106;
    private static final long serialVersionUID=-1711656903622072997L;
    private int reason;

    public PrintJobEvent(DocPrintJob source,int reason){
        super(source);
        this.reason=reason;
    }

    public int getPrintEventType(){
        return reason;
    }

    public DocPrintJob getPrintJob(){
        return (DocPrintJob)getSource();
    }
}
