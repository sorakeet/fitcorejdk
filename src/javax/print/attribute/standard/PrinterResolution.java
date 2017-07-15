/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.*;

public final class PrinterResolution extends ResolutionSyntax
        implements DocAttribute, PrintRequestAttribute, PrintJobAttribute{
    private static final long serialVersionUID=13090306561090558L;

    public PrinterResolution(int crossFeedResolution,int feedResolution,
                             int units){
        super(crossFeedResolution,feedResolution,units);
    }

    public boolean equals(Object object){
        return (super.equals(object)&&
                object instanceof PrinterResolution);
    }

    public final Class<? extends Attribute> getCategory(){
        return PrinterResolution.class;
    }

    public final String getName(){
        return "printer-resolution";
    }
}
