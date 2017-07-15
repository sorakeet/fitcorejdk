/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.*;

public final class SheetCollate extends EnumSyntax
        implements DocAttribute, PrintRequestAttribute, PrintJobAttribute{
    public static final SheetCollate UNCOLLATED=new SheetCollate(0);
    public static final SheetCollate COLLATED=new SheetCollate(1);
    private static final long serialVersionUID=7080587914259873003L;
    private static final String[] myStringTable={
            "uncollated",
            "collated"
    };
    private static final SheetCollate[] myEnumValueTable={
            UNCOLLATED,
            COLLATED
    };
    protected SheetCollate(int value){
        super(value);
    }

    protected String[] getStringTable(){
        return myStringTable;
    }

    protected EnumSyntax[] getEnumValueTable(){
        return myEnumValueTable;
    }

    public final Class<? extends Attribute> getCategory(){
        return SheetCollate.class;
    }

    public final String getName(){
        return "sheet-collate";
    }
}
