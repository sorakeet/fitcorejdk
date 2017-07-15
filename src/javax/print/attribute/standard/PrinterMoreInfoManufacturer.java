/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.URISyntax;
import java.net.URI;

public final class PrinterMoreInfoManufacturer extends URISyntax
        implements PrintServiceAttribute{
    private static final long serialVersionUID=3323271346485076608L;

    public PrinterMoreInfoManufacturer(URI uri){
        super(uri);
    }

    public boolean equals(Object object){
        return (super.equals(object)&&
                object instanceof PrinterMoreInfoManufacturer);
    }

    public final Class<? extends Attribute> getCategory(){
        return PrinterMoreInfoManufacturer.class;
    }

    public final String getName(){
        return "printer-more-info-manufacturer";
    }
}
