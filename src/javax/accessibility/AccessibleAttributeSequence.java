/**
 * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.accessibility;

import javax.swing.text.AttributeSet;

public class AccessibleAttributeSequence{
    public int startIndex;
    public int endIndex;
    public AttributeSet attributes;

    public AccessibleAttributeSequence(int start,int end,AttributeSet attr){
        startIndex=start;
        endIndex=end;
        attributes=attr;
    }
};
