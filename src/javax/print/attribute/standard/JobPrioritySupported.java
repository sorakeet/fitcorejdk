/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.IntegerSyntax;
import javax.print.attribute.SupportedValuesAttribute;

public final class JobPrioritySupported extends IntegerSyntax
        implements SupportedValuesAttribute{
    private static final long serialVersionUID=2564840378013555894L;

    public JobPrioritySupported(int value){
        super(value,1,100);
    }

    public boolean equals(Object object){
        return (super.equals(object)&&
                object instanceof JobPrioritySupported);
    }

    public final Class<? extends Attribute> getCategory(){
        return JobPrioritySupported.class;
    }

    public final String getName(){
        return "job-priority-supported";
    }
}
