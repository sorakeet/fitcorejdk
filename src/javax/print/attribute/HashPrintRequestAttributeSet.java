/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute;

import java.io.Serializable;

public class HashPrintRequestAttributeSet extends HashAttributeSet
        implements PrintRequestAttributeSet, Serializable{
    private static final long serialVersionUID=2364756266107751933L;

    public HashPrintRequestAttributeSet(){
        super(PrintRequestAttribute.class);
    }

    public HashPrintRequestAttributeSet(PrintRequestAttribute attribute){
        super(attribute,PrintRequestAttribute.class);
    }

    public HashPrintRequestAttributeSet(PrintRequestAttribute[] attributes){
        super(attributes,PrintRequestAttribute.class);
    }

    public HashPrintRequestAttributeSet(PrintRequestAttributeSet attributes){
        super(attributes,PrintRequestAttribute.class);
    }
}
