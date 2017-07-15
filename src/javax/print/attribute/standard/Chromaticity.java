/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.*;

public final class Chromaticity extends EnumSyntax
        implements DocAttribute, PrintRequestAttribute, PrintJobAttribute{
    public static final Chromaticity MONOCHROME=new Chromaticity(0);
    public static final Chromaticity COLOR=new Chromaticity(1);
    private static final long serialVersionUID=4660543931355214012L;
    private static final String[] myStringTable={"monochrome",
            "color"};
    private static final Chromaticity[] myEnumValueTable={MONOCHROME,
            COLOR};
    protected Chromaticity(int value){
        super(value);
    }

    protected String[] getStringTable(){
        return myStringTable;
    }

    protected EnumSyntax[] getEnumValueTable(){
        return myEnumValueTable;
    }

    public final Class<? extends Attribute> getCategory(){
        return Chromaticity.class;
    }

    public final String getName(){
        return "chromaticity";
    }
}
