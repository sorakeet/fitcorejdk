/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.TextSyntax;
import java.util.Locale;

public final class PrinterLocation extends TextSyntax
        implements PrintServiceAttribute{
    private static final long serialVersionUID=-1598610039865566337L;

    public PrinterLocation(String location,Locale locale){
        super(location,locale);
    }

    public boolean equals(Object object){
        return (super.equals(object)&&object instanceof PrinterLocation);
    }

    public final Class<? extends Attribute> getCategory(){
        return PrinterLocation.class;
    }

    public final String getName(){
        return "printer-location";
    }
}
