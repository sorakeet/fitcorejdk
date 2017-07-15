/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.PrintJobAttribute;
import javax.print.attribute.TextSyntax;
import java.util.Locale;

public final class OutputDeviceAssigned extends TextSyntax
        implements PrintJobAttribute{
    private static final long serialVersionUID=5486733778854271081L;

    public OutputDeviceAssigned(String deviceName,Locale locale){
        super(deviceName,locale);
    }
    // Exported operations inherited and overridden from class Object.

    public boolean equals(Object object){
        return (super.equals(object)&&
                object instanceof OutputDeviceAssigned);
    }

    public final Class<? extends Attribute> getCategory(){
        return OutputDeviceAssigned.class;
    }

    public final String getName(){
        return "output-device-assigned";
    }
}
