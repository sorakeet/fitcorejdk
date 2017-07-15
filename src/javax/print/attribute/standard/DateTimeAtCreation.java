/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.DateTimeSyntax;
import javax.print.attribute.PrintJobAttribute;
import java.util.Date;

public final class DateTimeAtCreation extends DateTimeSyntax
        implements PrintJobAttribute{
    private static final long serialVersionUID=-2923732231056647903L;

    public DateTimeAtCreation(Date dateTime){
        super(dateTime);
    }

    public boolean equals(Object object){
        return (super.equals(object)&&
                object instanceof DateTimeAtCreation);
    }

    public final Class<? extends Attribute> getCategory(){
        return DateTimeAtCreation.class;
    }

    public final String getName(){
        return "date-time-at-creation";
    }
}
