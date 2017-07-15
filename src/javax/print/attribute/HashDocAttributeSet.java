/**
 * Copyright (c) 2000, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute;

import java.io.Serializable;

public class HashDocAttributeSet extends HashAttributeSet
        implements DocAttributeSet, Serializable{
    private static final long serialVersionUID=-1128534486061432528L;

    public HashDocAttributeSet(){
        super(DocAttribute.class);
    }

    public HashDocAttributeSet(DocAttribute attribute){
        super(attribute,DocAttribute.class);
    }

    public HashDocAttributeSet(DocAttribute[] attributes){
        super(attributes,DocAttribute.class);
    }

    public HashDocAttributeSet(DocAttributeSet attributes){
        super(attributes,DocAttribute.class);
    }
}
