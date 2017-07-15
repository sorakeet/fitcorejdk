/**
 * Copyright (c) 1997, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import java.util.Enumeration;

public interface MutableAttributeSet extends AttributeSet{
    public void addAttribute(Object name,Object value);

    public void addAttributes(AttributeSet attributes);

    public void removeAttribute(Object name);

    public void removeAttributes(Enumeration<?> names);

    public void removeAttributes(AttributeSet attributes);

    public void setResolveParent(AttributeSet parent);
}
