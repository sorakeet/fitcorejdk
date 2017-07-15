/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.SetOfIntegerSyntax;
import javax.print.attribute.SupportedValuesAttribute;

public final class JobMediaSheetsSupported extends SetOfIntegerSyntax
        implements SupportedValuesAttribute{
    private static final long serialVersionUID=2953685470388672940L;

    public JobMediaSheetsSupported(int lowerBound,int upperBound){
        super(lowerBound,upperBound);
        if(lowerBound>upperBound){
            throw new IllegalArgumentException("Null range specified");
        }else if(lowerBound<0){
            throw new IllegalArgumentException
                    ("Job K octets value < 0 specified");
        }
    }

    public boolean equals(Object object){
        return (super.equals(object)&&
                object instanceof JobMediaSheetsSupported);
    }

    public final Class<? extends Attribute> getCategory(){
        return JobMediaSheetsSupported.class;
    }

    public final String getName(){
        return "job-media-sheets-supported";
    }
}
