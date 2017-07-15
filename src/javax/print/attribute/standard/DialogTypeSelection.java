/**
 * Copyright (c) 2003, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.EnumSyntax;
import javax.print.attribute.PrintRequestAttribute;

public final class DialogTypeSelection extends EnumSyntax
        implements PrintRequestAttribute{
    public static final DialogTypeSelection
            NATIVE=new DialogTypeSelection(0);
    public static final DialogTypeSelection
            COMMON=new DialogTypeSelection(1);
    private static final long serialVersionUID=7518682952133256029L;
    private static final String[] myStringTable={
            "native","common"};
    private static final DialogTypeSelection[] myEnumValueTable={
            NATIVE,
            COMMON
    };
    protected DialogTypeSelection(int value){
        super(value);
    }

    protected String[] getStringTable(){
        return myStringTable;
    }

    protected EnumSyntax[] getEnumValueTable(){
        return myEnumValueTable;
    }

    public final Class getCategory(){
        return DialogTypeSelection.class;
    }

    public final String getName(){
        return "dialog-type-selection";
    }
}
