/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.PrintJobAttribute;

public class JobState extends EnumSyntax implements PrintJobAttribute{
    public static final JobState UNKNOWN=new JobState(0);
    public static final JobState PENDING=new JobState(3);
    public static final JobState PENDING_HELD=new JobState(4);
    public static final JobState PROCESSING=new JobState(5);
    public static final JobState PROCESSING_STOPPED=new JobState(6);
    public static final JobState CANCELED=new JobState(7);
    public static final JobState ABORTED=new JobState(8);
    public static final JobState COMPLETED=new JobState(9);
    private static final long serialVersionUID=400465010094018920L;
    // Hidden constructors.
    private static final String[] myStringTable=
            {"unknown",
                    null,
                    null,
                    "pending",
                    "pending-held",
                    "processing",
                    "processing-stopped",
                    "canceled",
                    "aborted",
                    "completed"};
    private static final JobState[] myEnumValueTable=
            {UNKNOWN,
                    null,
                    null,
                    PENDING,
                    PENDING_HELD,
                    PROCESSING,
                    PROCESSING_STOPPED,
                    CANCELED,
                    ABORTED,
                    COMPLETED};
    protected JobState(int value){
        super(value);
    }

    protected String[] getStringTable(){
        return myStringTable;
    }

    protected EnumSyntax[] getEnumValueTable(){
        return myEnumValueTable;
    }

    public final Class<? extends Attribute> getCategory(){
        return JobState.class;
    }

    public final String getName(){
        return "job-state";
    }
}
