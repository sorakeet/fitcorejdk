/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.PrintJobAttribute;
import javax.print.attribute.PrintRequestAttribute;

public class JobSheets extends EnumSyntax
        implements PrintRequestAttribute, PrintJobAttribute{
    public static final JobSheets NONE=new JobSheets(0);
    public static final JobSheets STANDARD=new JobSheets(1);
    private static final long serialVersionUID=-4735258056132519759L;
    private static final String[] myStringTable={
            "none",
            "standard"
    };
    private static final JobSheets[] myEnumValueTable={
            NONE,
            STANDARD
    };
    protected JobSheets(int value){
        super(value);
    }

    protected String[] getStringTable(){
        return (String[])myStringTable.clone();
    }

    protected EnumSyntax[] getEnumValueTable(){
        return (EnumSyntax[])myEnumValueTable.clone();
    }

    public final Class<? extends Attribute> getCategory(){
        return JobSheets.class;
    }

    public final String getName(){
        return "job-sheets";
    }
}
