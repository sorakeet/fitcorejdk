/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.PrintServiceAttribute;

public class PDLOverrideSupported extends EnumSyntax
        implements PrintServiceAttribute{
    public static final PDLOverrideSupported
            NOT_ATTEMPTED=new PDLOverrideSupported(0);
    public static final PDLOverrideSupported
            ATTEMPTED=new PDLOverrideSupported(1);
    private static final long serialVersionUID=-4393264467928463934L;
    private static final String[] myStringTable={
            "not-attempted",
            "attempted"
    };
    private static final PDLOverrideSupported[] myEnumValueTable={
            NOT_ATTEMPTED,
            ATTEMPTED
    };
    protected PDLOverrideSupported(int value){
        super(value);
    }

    protected String[] getStringTable(){
        return (String[])myStringTable.clone();
    }

    protected EnumSyntax[] getEnumValueTable(){
        return (EnumSyntax[])myEnumValueTable.clone();
    }

    public final Class<? extends Attribute> getCategory(){
        return PDLOverrideSupported.class;
    }

    public final String getName(){
        return "pdl-override-supported";
    }
}
