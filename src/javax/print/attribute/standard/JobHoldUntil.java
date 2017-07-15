/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.DateTimeSyntax;
import javax.print.attribute.PrintJobAttribute;
import javax.print.attribute.PrintRequestAttribute;
import java.util.Date;

public final class JobHoldUntil extends DateTimeSyntax
        implements PrintRequestAttribute, PrintJobAttribute{
    private static final long serialVersionUID=-1664471048860415024L;

    public JobHoldUntil(Date dateTime){
        super(dateTime);
    }

    public boolean equals(Object object){
        return (super.equals(object)&&object instanceof JobHoldUntil);
    }

    public final Class<? extends Attribute> getCategory(){
        return JobHoldUntil.class;
    }

    public final String getName(){
        return "job-hold-until";
    }
}
