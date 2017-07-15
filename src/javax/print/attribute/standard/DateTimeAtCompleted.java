/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.DateTimeSyntax;
import javax.print.attribute.PrintJobAttribute;
import java.util.Date;

public final class DateTimeAtCompleted extends DateTimeSyntax
        implements PrintJobAttribute{
    private static final long serialVersionUID=6497399708058490000L;

    public DateTimeAtCompleted(Date dateTime){
        super(dateTime);
    }

    public boolean equals(Object object){
        return (super.equals(object)&&
                object instanceof DateTimeAtCompleted);
    }
// Exported operations inherited and implemented from interface Attribute.

    public final Class<? extends Attribute> getCategory(){
        return DateTimeAtCompleted.class;
    }

    public final String getName(){
        return "date-time-at-completed";
    }
}
