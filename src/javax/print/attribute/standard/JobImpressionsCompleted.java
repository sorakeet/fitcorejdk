/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.IntegerSyntax;
import javax.print.attribute.PrintJobAttribute;

public final class JobImpressionsCompleted extends IntegerSyntax
        implements PrintJobAttribute{
    private static final long serialVersionUID=6722648442432393294L;

    public JobImpressionsCompleted(int value){
        super(value,0,Integer.MAX_VALUE);
    }

    public boolean equals(Object object){
        return (super.equals(object)&&
                object instanceof JobImpressionsCompleted);
    }

    public final Class<? extends Attribute> getCategory(){
        return JobImpressionsCompleted.class;
    }

    public final String getName(){
        return "job-impressions-completed";
    }
}
