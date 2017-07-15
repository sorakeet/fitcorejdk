/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.DocAttribute;
import javax.print.attribute.EnumSyntax;

public class Compression extends EnumSyntax implements DocAttribute{
    public static final Compression NONE=new Compression(0);
    public static final Compression DEFLATE=new Compression(1);
    public static final Compression GZIP=new Compression(2);
    public static final Compression COMPRESS=new Compression(3);
    private static final long serialVersionUID=-5716748913324997674L;
    private static final String[] myStringTable={"none",
            "deflate",
            "gzip",
            "compress"};
    private static final Compression[] myEnumValueTable={NONE,
            DEFLATE,
            GZIP,
            COMPRESS};
    protected Compression(int value){
        super(value);
    }

    protected String[] getStringTable(){
        return (String[])myStringTable.clone();
    }

    protected EnumSyntax[] getEnumValueTable(){
        return (EnumSyntax[])myEnumValueTable.clone();
    }

    public final Class<? extends Attribute> getCategory(){
        return Compression.class;
    }

    public final String getName(){
        return "compression";
    }
}
