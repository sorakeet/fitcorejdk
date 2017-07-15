/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.URISyntax;
import java.net.URI;

public final class PrinterMoreInfo extends URISyntax
        implements PrintServiceAttribute{
    private static final long serialVersionUID=4555850007675338574L;

    public PrinterMoreInfo(URI uri){
        super(uri);
    }

    public boolean equals(Object object){
        return (super.equals(object)&&
                object instanceof PrinterMoreInfo);
    }

    public final Class<? extends Attribute> getCategory(){
        return PrinterMoreInfo.class;
    }

    public final String getName(){
        return "printer-more-info";
    }
}
