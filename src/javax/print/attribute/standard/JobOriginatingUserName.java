/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.PrintJobAttribute;
import javax.print.attribute.TextSyntax;
import java.util.Locale;

public final class JobOriginatingUserName extends TextSyntax
        implements PrintJobAttribute{
    private static final long serialVersionUID=-8052537926362933477L;

    public JobOriginatingUserName(String userName,Locale locale){
        super(userName,locale);
    }

    public boolean equals(Object object){
        return (super.equals(object)&&
                object instanceof JobOriginatingUserName);
    }

    public final Class<? extends Attribute> getCategory(){
        return JobOriginatingUserName.class;
    }

    public final String getName(){
        return "job-originating-user-name";
    }
}
