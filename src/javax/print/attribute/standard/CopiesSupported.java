/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.SetOfIntegerSyntax;
import javax.print.attribute.SupportedValuesAttribute;

public final class CopiesSupported extends SetOfIntegerSyntax
        implements SupportedValuesAttribute{
    private static final long serialVersionUID=6927711687034846001L;

    public CopiesSupported(int member){
        super(member);
        if(member<1){
            throw new IllegalArgumentException("Copies value < 1 specified");
        }
    }

    public CopiesSupported(int lowerBound,int upperBound){
        super(lowerBound,upperBound);
        if(lowerBound>upperBound){
            throw new IllegalArgumentException("Null range specified");
        }else if(lowerBound<1){
            throw new IllegalArgumentException("Copies value < 1 specified");
        }
    }

    public boolean equals(Object object){
        return super.equals(object)&&object instanceof CopiesSupported;
    }

    public final Class<? extends Attribute> getCategory(){
        return CopiesSupported.class;
    }

    public final String getName(){
        return "copies-supported";
    }
}
