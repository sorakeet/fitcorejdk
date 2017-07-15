/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.IntegerSyntax;
import javax.print.attribute.PrintJobAttribute;
import javax.print.attribute.PrintRequestAttribute;

public final class Copies extends IntegerSyntax
        implements PrintRequestAttribute, PrintJobAttribute{
    private static final long serialVersionUID=-6426631521680023833L;

    public Copies(int value){
        super(value,1,Integer.MAX_VALUE);
    }

    public boolean equals(Object object){
        return super.equals(object)&&object instanceof Copies;
    }

    public final Class<? extends Attribute> getCategory(){
        return Copies.class;
    }

    public final String getName(){
        return "copies";
    }
}
