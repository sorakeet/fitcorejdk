/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import java.util.Enumeration;

public interface AttributeSet{
    public static final Object NameAttribute=StyleConstants.NameAttribute;
    public static final Object ResolveAttribute=StyleConstants.ResolveAttribute;

    public int getAttributeCount();

    public boolean isDefined(Object attrName);

    public boolean isEqual(AttributeSet attr);

    public AttributeSet copyAttributes();

    public Object getAttribute(Object key);

    public Enumeration<?> getAttributeNames();

    public boolean containsAttribute(Object name,Object value);

    public boolean containsAttributes(AttributeSet attributes);

    public AttributeSet getResolveParent();

    public interface FontAttribute{
    }

    public interface ColorAttribute{
    }

    public interface CharacterAttribute{
    }
    public interface ParagraphAttribute{
    }
}
