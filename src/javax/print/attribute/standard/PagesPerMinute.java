/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.IntegerSyntax;
import javax.print.attribute.PrintServiceAttribute;

public final class PagesPerMinute extends IntegerSyntax
        implements PrintServiceAttribute{
    private static final long serialVersionUID=-6366403993072862015L;

    public PagesPerMinute(int value){
        super(value,0,Integer.MAX_VALUE);
    }

    public boolean equals(Object object){
        return (super.equals(object)&&
                object instanceof PagesPerMinute);
    }

    public final Class<? extends Attribute> getCategory(){
        return PagesPerMinute.class;
    }

    public final String getName(){
        return "pages-per-minute";
    }
}
