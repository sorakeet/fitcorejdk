/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.TextSyntax;
import java.util.Locale;

public final class PrinterName extends TextSyntax
        implements PrintServiceAttribute{
    private static final long serialVersionUID=299740639137803127L;

    public PrinterName(String printerName,Locale locale){
        super(printerName,locale);
    }

    public boolean equals(Object object){
        return (super.equals(object)&&object instanceof PrinterName);
    }

    public final Class<? extends Attribute> getCategory(){
        return PrinterName.class;
    }

    public final String getName(){
        return "printer-name";
    }
}
