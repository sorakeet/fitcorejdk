/**
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.PrintJobAttribute;
import javax.print.attribute.PrintRequestAttribute;

public final class Fidelity extends EnumSyntax
        implements PrintJobAttribute, PrintRequestAttribute{
    public static final Fidelity
            FIDELITY_TRUE=new Fidelity(0);
    public static final Fidelity
            FIDELITY_FALSE=new Fidelity(1);
    private static final long serialVersionUID=6320827847329172308L;
    private static final String[] myStringTable={
            "true",
            "false"
    };
    private static final Fidelity[] myEnumValueTable={
            FIDELITY_TRUE,
            FIDELITY_FALSE
    };
    protected Fidelity(int value){
        super(value);
    }

    protected String[] getStringTable(){
        return myStringTable;
    }

    protected EnumSyntax[] getEnumValueTable(){
        return myEnumValueTable;
    }

    public final Class<? extends Attribute> getCategory(){
        return Fidelity.class;
    }

    public final String getName(){
        return "ipp-attribute-fidelity";
    }
}
