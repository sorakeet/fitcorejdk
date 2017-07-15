/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute;

import java.io.Serializable;

public class HashPrintServiceAttributeSet extends HashAttributeSet
        implements PrintServiceAttributeSet, Serializable{
    private static final long serialVersionUID=6642904616179203070L;

    public HashPrintServiceAttributeSet(){
        super(PrintServiceAttribute.class);
    }

    public HashPrintServiceAttributeSet(PrintServiceAttribute attribute){
        super(attribute,PrintServiceAttribute.class);
    }

    public HashPrintServiceAttributeSet(PrintServiceAttribute[] attributes){
        super(attributes,PrintServiceAttribute.class);
    }

    public HashPrintServiceAttributeSet(PrintServiceAttributeSet attributes){
        super(attributes,PrintServiceAttribute.class);
    }
}
