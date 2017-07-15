/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.*;

public class PrintQuality extends EnumSyntax
        implements DocAttribute, PrintRequestAttribute, PrintJobAttribute{
    public static final PrintQuality DRAFT=new PrintQuality(3);
    public static final PrintQuality NORMAL=new PrintQuality(4);
    public static final PrintQuality HIGH=new PrintQuality(5);
    private static final long serialVersionUID=-3072341285225858365L;
    private static final String[] myStringTable={
            "draft",
            "normal",
            "high"
    };
    private static final PrintQuality[] myEnumValueTable={
            DRAFT,
            NORMAL,
            HIGH
    };
    protected PrintQuality(int value){
        super(value);
    }

    protected String[] getStringTable(){
        return (String[])myStringTable.clone();
    }

    protected EnumSyntax[] getEnumValueTable(){
        return (EnumSyntax[])myEnumValueTable.clone();
    }

    protected int getOffset(){
        return 3;
    }

    public final Class<? extends Attribute> getCategory(){
        return PrintQuality.class;
    }

    public final String getName(){
        return "print-quality";
    }
}
