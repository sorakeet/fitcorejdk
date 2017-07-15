/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.IntegerSyntax;
import javax.print.attribute.PrintJobAttribute;
import javax.print.attribute.PrintRequestAttribute;

public final class JobKOctets extends IntegerSyntax
        implements PrintRequestAttribute, PrintJobAttribute{
    private static final long serialVersionUID=-8959710146498202869L;

    public JobKOctets(int value){
        super(value,0,Integer.MAX_VALUE);
    }

    public boolean equals(Object object){
        return super.equals(object)&&object instanceof JobKOctets;
    }

    public final Class<? extends Attribute> getCategory(){
        return JobKOctets.class;
    }

    public final String getName(){
        return "job-k-octets";
    }
}
