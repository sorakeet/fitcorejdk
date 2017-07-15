/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.EnumSyntax;

public class MediaName extends Media implements Attribute{
    public static final MediaName NA_LETTER_WHITE=new MediaName(0);
    public static final MediaName NA_LETTER_TRANSPARENT=new MediaName(1);
    public static final MediaName ISO_A4_WHITE=new MediaName(2);
    public static final MediaName ISO_A4_TRANSPARENT=new MediaName(3);
    private static final long serialVersionUID=4653117714524155448L;
    private static final String[] myStringTable={
            "na-letter-white",
            "na-letter-transparent",
            "iso-a4-white",
            "iso-a4-transparent"
    };
    private static final MediaName[] myEnumValueTable={
            NA_LETTER_WHITE,
            NA_LETTER_TRANSPARENT,
            ISO_A4_WHITE,
            ISO_A4_TRANSPARENT
    };
    protected MediaName(int value){
        super(value);
    }

    protected String[] getStringTable(){
        return (String[])myStringTable.clone();
    }

    protected EnumSyntax[] getEnumValueTable(){
        return (EnumSyntax[])myEnumValueTable.clone();
    }
}
